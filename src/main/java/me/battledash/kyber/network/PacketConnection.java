package me.battledash.kyber.network;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.Tickable;
import me.battledash.kyber.engine.simulation.input.EntryInputStateNetworkMove;
import me.battledash.kyber.engine.simulation.input.MoveObject;
import me.battledash.kyber.network.stream.managers.StreamManagerChat;
import me.battledash.kyber.network.stream.managers.ghost.ServerGhost;
import me.battledash.kyber.runtime.GameTime;
import me.battledash.kyber.server.GameContext;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.streams.BitStreamWrite;
import me.battledash.kyber.streams.InBitStream;
import me.battledash.kyber.streams.OutBitStream;
import me.battledash.kyber.types.network.Initiator;
import me.battledash.kyber.types.network.messages.NetworkTimeSyncMessage;
import me.battledash.kyber.network.packet.CommandType;
import me.battledash.kyber.network.packet.DisconnectReason;
import me.battledash.kyber.network.packet.FrostbitePacket;
import me.battledash.kyber.network.stream.StreamControl;
import me.battledash.kyber.network.stream.StreamManagerEngine;
import me.battledash.kyber.network.packet.HandshakingState;
import me.battledash.kyber.network.packet.PacketType;
import me.battledash.kyber.network.stream.managers.StreamManagerDummy;
import me.battledash.kyber.network.stream.managers.StreamManagerFile;
import me.battledash.kyber.network.stream.managers.ghost.ClientGhost;
import me.battledash.kyber.network.stream.managers.ghost.GhostConnection;
import me.battledash.kyber.network.stream.managers.ghost.StreamManagerGhost;
import me.battledash.kyber.network.stream.managers.StreamManagerMoveClient;
import me.battledash.kyber.network.stream.managers.StreamManagerMoveServer;
import me.battledash.kyber.network.stream.managers.message.NetworkableMessage;
import me.battledash.kyber.network.stream.managers.message.StreamManagerMessage;
import me.battledash.kyber.server.ServerGameContext;
import org.apache.commons.codec.binary.Hex;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
public abstract class PacketConnection implements Tickable {

    private static final PacketType[] PACKET_TYPES = PacketType.values();

    private static final int MAX_PACKET_SIZE = 1600;

    protected final PacketHandler handler;
    private final InetSocketAddress address;
    private final int connectionId;
    private short channelId;
    private short peerChannelId;
    private long validLocalPlayersMask;
    private long clientNonce;
    private long lastReceiveTime;

    private HandshakingState state = HandshakingState.HANDSHAKE;
    private Queue<InBitStream> receiveQueue = new LinkedList<>();
    private PacketNotification packetNotification = new PacketNotification();

    private StreamControl streamControl;
    private StreamManagerMessage messageManager;
    private StreamManagerChat chatManager;
    private StreamManagerGhost ghostManager;
    private GhostConnectionClient ghostConnection = new GhostConnectionClient();

    public void init() {
        for (int i = 0; i < 2; i++) {
            if ((this.validLocalPlayersMask & (1 << i)) > 0) {
                log.info("Initializing stream for player {}", i);
            }
        }

        this.messageManager = new StreamManagerMessage(this, this.ghostConnection);
        this.chatManager = new StreamManagerChat();
        this.ghostManager = new StreamManagerGhost();
        this.ghostManager.setGhostConnection(this.ghostConnection);
    }

    public void disconnected() {

    }

    public void sendAccept() {
        this.state = HandshakingState.SEND_ACCEPT;

        OutBitStream outStream = OutBitStream.init();
        outStream.write(this.clientNonce, 32);
        outStream.write(this.channelId, 16);
        this.sendPacket(false, CommandType.ACCEPT, outStream);

        this.setLastReceiveTime(System.currentTimeMillis());
    }

    public void sendDisconnect(DisconnectReason reason, String message) {
        OutBitStream stream = OutBitStream.init();
        stream.write(reason.ordinal(), 32);
        stream.writeString(message);
        for (int i = 0; i < 4; i++) {
            this.sendPacket(true, CommandType.DISCONNECT, stream);
        }
    }

    public void onAcceptConfirmation(InBitStream data) {
        byte[] buffer = data.readOctets((int) data.read(8));
        log.info("Received accept confirmation {}", buffer.length * 8);
        InBitStream stream = new InBitStream();
        stream.initBits(Unpooled.wrappedBuffer(buffer), buffer.length * 8);

        long clientNonce = stream.read(32);
        if (this.clientNonce == clientNonce) {
            long acNonce = stream.read(32);
            this.peerChannelId = (short) stream.read(16);
            log.info("Accepted connection from {} with acNonce {} and peer channel {} ({})", clientNonce, acNonce, this.peerChannelId, Hex.encodeHexString(buffer));
            this.confirmConnection(acNonce);
        }
    }

    public void confirmConnection(long acNonce) {
        this.streamControl = new StreamControl(this, false);
        this.streamControl.setManager(this.streamControl.getFrameManager());
        this.initManagers();

        this.state = HandshakingState.PLAY;
        this.sendConnectionConfirmed(acNonce);
    }

    private void initManagers() {
        StreamManagerEngine streamManager = this.streamControl.getStreamManager();
        if (GameContext.SIMULATE_CLIENT) {
            streamManager.addManager(new StreamManagerMoveClient());
        } else {
            streamManager.addManager(new StreamManagerMoveServer(this, new EntryInputStateNetworkMove()));
        }

        streamManager.addManager(new StreamManagerDummy("StreamManagerDamage"));
        streamManager.addManager(this.messageManager);
        streamManager.addManager(new StreamManagerDummy("Destruction"));

        streamManager.addManager(new StreamManagerDummy("AutoPlayers"));
        streamManager.addManager(this.chatManager);
        streamManager.addManager(new StreamManagerFile());
        streamManager.addManager(this.ghostManager);
        streamManager.addManager(new StreamManagerDummy("Telemetry"));
    }

    public void sendConnectionConfirmed(long acNonce) {
        OutBitStream outStream = OutBitStream.init(256);
        outStream.write(acNonce, 32);
        int data = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
        outStream.write(data, 32);
        outStream.flush();

        OutBitStream packetStream = OutBitStream.init();
        packetStream.write(8, 8);
        packetStream.writeOctets(outStream.getBuffer(), 8);
        packetStream.flush();
        log.debug("Sending connection confirmed {}, {}, {}", acNonce, data,
                ByteBufUtil.hexDump(packetStream.getBuffer().copy(0, packetStream.getOctetCount())));
        this.sendPacket(true, CommandType.CONNECTION_CONFIRMED, packetStream);

        this.onConnected();
    }

    public abstract void onConnected();

    public void onReceive(InBitStream stream) {
        long startPos = stream.getStreamBitReadPosition();
        boolean internalPacket = stream.read(1) == 1;

        if (internalPacket) {
            PacketType type = PacketConnection.PACKET_TYPES[(int) stream.read(4)];
            switch (type) {
                case PING_REQUEST -> this.onPingRequestPacket(stream);
                default -> log.info("Internal packet: {}", type);
            }
            this.packetNotification.packetReceivedAndVerified(true);
            return;
        } else {
            log.debug("Packet size: {}", stream.getBufferBitSize() / 8);
        }

        stream.seek((int) startPos);
        this.receiveQueue.add(stream);
        int lostIncomingPacketCount = this.packetNotification.packetReceivedAndVerified(true);
        // TODO: 5/7/2022 Create Packet structure and set lost count after adding to queue
    }

    private void writeTime(OutBitStream stream, double time) {
        long unsignedTime = Double.doubleToLongBits(time);
        stream.write(unsignedTime >> 32L, 32);
        stream.write(unsignedTime, 32);
    }

    private double readTime(InBitStream stream) {
        long unsignedTime = stream.read(32);
        unsignedTime <<= 32;
        unsignedTime |= stream.read(32);
        return Double.longBitsToDouble(unsignedTime);
    }

    private void onPingRequestPacket(InBitStream stream) {
        double time = this.readTime(stream);

        log.debug("Ping request: {}", time);
        this.sendPingResponse(time);
    }

    private void sendPingResponse(double timestamp) {
        OutBitStream stream = OutBitStream.init();

        this.writeTime(stream, timestamp);
        this.writeTime(stream, timestamp);

        this.sendPacket(PacketType.PING_RESPONSE, stream);
    }

    public void processReceivedPackets() {
        log.debug("Ticking connection {}, processing {} queued packets", this.channelId, this.receiveQueue.size());
        InBitStream packet;
        while ((packet = this.receiveQueue.poll()) != null) {
            /*OutBitStream outStream = OutBitStream.init(PacketConnection.MAX_PACKET_SIZE);
            outStream.writeStream(packet);
            outStream.flush();
            InBitStream inStream = new InBitStream();
            inStream.initBits(outStream.getBuffer(), outStream.getStreamBitWritePosition());
            inStream.seek(0);*/
            InBitStream inStream = packet;
            inStream.read(1);
            log.debug("Received packet {} ({})", inStream.getHexString(), inStream.getBufferBitSize());
            this.streamControl.internalReceiveBitStream(inStream);
        }
    }

    public void tick() {
        log.debug("Ticking connection");
        if (this.streamControl != null) {
            this.streamControl.tick();
        }

        if (this.ghostManager != null) {
            this.ghostManager.setServerFrameTime(ServerGameContext.context().getGameTime().getTime());
        }

        this.processReceivedPackets();
    }

    public ChannelFuture sendPacket(boolean useChannel, CommandType command, OutBitStream stream) {
        return this.getHandler().getChannel().write(new FrostbitePacket<>(useChannel ? this.peerChannelId : 0, command, stream, this.address));
    }

    public ChannelFuture sendPacket(PacketType type, OutBitStream stream) {
        OutBitStream dataStream = this.initDataStream();

        this.writePacketHeader(dataStream, type);

        stream.flush();
        dataStream.writeStream(stream.convertToRead());
        dataStream.flush();

        return this.getHandler().getChannel().write(new FrostbitePacket<>(this.peerChannelId, CommandType.DATA, dataStream, this.address));
    }

    public OutBitStream initDataStream() {
        OutBitStream stream = OutBitStream.init();

        this.packetNotification.advancePacketSequenceNumber();
        this.packetNotification.writeHeader(stream);

        int pos = stream.getStreamBitWritePosition() + PacketDecoder.HEADER_SIZE;
        int diff = (8 - (pos & 7)) & 7;
        stream.write(0, diff);
        stream.write(0, (int) PacketHandler.CHECKSUM_SIZE * 8);

        return stream;
    }

    public void writePacketHeader(OutBitStream stream, PacketType type) {
        stream.write(type == PacketType.DATA ? 0 : 1, 1);
        if (type != PacketType.DATA) {
            stream.write(type.ordinal(), 4);
        }
    }

    public void timeSync(boolean triggerTimeSync) {
        GameTime gameTime = ServerGameContext.context().getGameTime();

        NetworkTimeSyncMessage msg = new NetworkTimeSyncMessage();
        msg.setTriggerTimeSync(triggerTimeSync);
        msg.setTicks(gameTime.getTicks());
        msg.setTimeCastToDouble(gameTime.getTime());

        this.sendMessage(msg);
    }

    public void sendMessageUnchecked(final NetworkableMessage message) {
        if (message.getInitiator() == Initiator.CLIENT) {
            throw new IllegalArgumentException("You cannot send a client-initiated message from the server");
        }

        if (GameContext.DISABLE_PACKET_TRANSFER) {
            return;
        }

        this.messageManager.addMessage(message);
    }

    public void sendMessage(final NetworkableMessage message) {
        if (!message.isHasNetworkedResources()) {
            this.sendMessageUnchecked(message);
            return;
        }

        throw new UnsupportedOperationException("Not yet implemented");
    }

    public MoveObject cloneMove(MoveObject object) {
        return new EntryInputStateNetworkMove((EntryInputStateNetworkMove) object);
    }

    public class GhostConnectionClient implements GhostConnection {

        @Override
        public ClientGhost readGhostId(BitStreamRead stream) {
            return PacketConnection.this.getGhostManager().readGhostId(stream);
        }

        @Override
        public void writeGhostId(BitStreamWrite stream, ServerGhost ghost) {
            PacketConnection.this.getGhostManager().writeGhostId(stream, ghost);
        }
    }

}
