package me.battledash.kyber.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.Tickable;
import me.battledash.kyber.server.GameContext;
import me.battledash.kyber.streams.InBitStream;
import me.battledash.kyber.streams.OutBitStream;
import me.battledash.kyber.network.packet.FrostbitePacket;
import me.battledash.kyber.network.packet.CommandType;
import me.battledash.kyber.network.packet.DisconnectReason;
import me.battledash.kyber.network.packet.HandshakingState;
import me.battledash.kyber.util.BufferUtil;
import me.battledash.kyber.util.MathUtil;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
public class PacketHandler extends SimpleChannelInboundHandler<FrostbitePacket<InBitStream>> implements Tickable {

    private static final DisconnectReason[] DISCONNECT_REASONS = DisconnectReason.values();

    protected static final long DATA_HEADER_SIZE = ((PacketDecoder.PADDING_BITS +
            PacketDecoder.CHANNEL_BITS +
            PacketDecoder.COMMAND_BITS +
            PacketNotification.SEQUENCE_NUMBER_BITS +
            PacketNotification.SEQUENCE_NUMBER_BITS +
            PacketNotification.SIZE + 7) / 8);

    protected static final long CHECKSUM_SIZE = 2;

    private final long challengeNumber = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);

    private final Set<ServerConnection> connections = new HashSet<>();

    private final ConnectionManager connectionManager;
    private Channel channel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FrostbitePacket<InBitStream> packet) {
        this.processPacket(packet);
    }

    public void processPacket(FrostbitePacket<InBitStream> packet) {
        CommandType command = packet.getCommand();
        log.debug("Received packet with channel ID {} and command {}", packet.getChannelID(), command);
        if (packet.getChannelID() == 0) {
            switch (command) {
                case CHALLENGE -> this.onChallenge(packet);
                case CUSTOM_CHALLENGE_RESPONSE -> this.onCustomChallengeResponse(packet);
                case CONNECT -> this.onConnect(packet);
            }
        } else {
            InBitStream data = packet.getData();
            if (command == CommandType.REMOTE_DISCONNECT) {
                //DisconnectReason reason = PacketHandler.DISCONNECT_REASONS[(int) packet.getData().read(32)];
                long reason = data.read(32);
                String reasonText = data.readString();
                log.info("Disconnect received from {} with reason {} ({})", packet.getAddress(), reason, reasonText);
                return;
            }

            ServerConnection connection = this.connections.stream()
                    .filter(c -> c.getChannelId() == packet.getChannelID())
                    .findFirst()
                    .orElse(null);

            if (connection == null) {
                if (command == CommandType.DISCONNECT) {
                    data.read(32);
                    DisconnectReason reason = DisconnectReason.CONNECT_FAILED;//PacketHandler.DISCONNECT_REASONS[(int) data.read(32)];
                    String reasonText = data.readString();
                    log.info("Disconnect received from {} with reason {} ({})", packet.getAddress(), reason, reasonText);
                } else {
                    log.warn("Received packet with channel ID {} but no connection exists", packet.getChannelID());
                }
                return;
            }

            connection.setLastReceiveTime(System.currentTimeMillis());

            if (connection.getState() == HandshakingState.PLAY || command == CommandType.DATA) {
                if (command == CommandType.DISCONNECT) {
                    log.info("Disconnecting client {}", connection.getAddress());
                    connection.unsafeDisconnect(DisconnectReason.GENERIC_ERROR, "Client disconnected");
                } else if (command == CommandType.DATA) {
                    PacketNotification.PacketSequenceStatus status = connection.getPacketNotification().readHeader(data);

                    // When simulating the client and processing fake packets, they
                    // contain sequence numbers from an old network session, so if
                    // we get an invalid sequence number, we'll just ignore it
                    if (status == PacketNotification.PacketSequenceStatus.INVALID && !GameContext.SIMULATE_CLIENT) {
                        log.error("Invalid packet sequence number received from {}, dropping connection", connection.getAddress());
                        connection.kick(DisconnectReason.MALFORMED_PACKET, "Invalid packet sequence number");
                        return;
                    }

                    long pos = data.getStreamBitReadPosition();
                    long diff = (8L - (pos & 7L)) & 7L;
                    data.skip((int) diff);

                    if (data.getStreamBitReadPosition() != PacketHandler.DATA_HEADER_SIZE * 8) {
                        log.warn("Data packet header size mismatch");
                        return;
                    }

                    // TODO: 4/4/2022 Validate checksum

                    InBitStream dataStream = new InBitStream();
                    ByteBuf original = data.getBuffer().retainedDuplicate().readerIndex(0);
                    int receivedChecksum = original.getUnsignedShortLE((int) PacketHandler.DATA_HEADER_SIZE);
                    log.debug("Received checksum {}", receivedChecksum);
                    int calculatedChecksum = BufferUtil.calcFletcher16(original.readableBytes(), original);
                    log.debug("Calculated checksum {}", calculatedChecksum);
                    original.setZero((int) PacketHandler.DATA_HEADER_SIZE, (int) PacketHandler.CHECKSUM_SIZE);

                    int index = (int) (PacketHandler.DATA_HEADER_SIZE + PacketHandler.CHECKSUM_SIZE);
                    dataStream.initBits(original.copy(index, original.capacity() - index),
                            (int) (data.getRemainingBitCount() - PacketHandler.CHECKSUM_SIZE * 8));

                    connection.onReceive(dataStream);
                } else {
                    log.info("Received packet with command {} but client is in state {}", command, connection.getState());
                }
            } else if (connection.getState() == HandshakingState.SEND_ACCEPT &&
                    command == CommandType.ACCEPT_CONFIRMATION) {
                connection.onAcceptConfirmation(data);
            }
        }
    }

    private void onChallenge(FrostbitePacket<InBitStream> packet) {
        InBitStream data = packet.getData();
        long protocol = data.read(32);
        long titleId = data.read(32);
        log.info("Client protocol: {}, titleId: {}", protocol, titleId);
        OutBitStream outStream = OutBitStream.init();
        outStream.write(ThreadLocalRandom.current().nextInt(), 32);
        outStream.write(MathUtil.selGtz(0, 1, 0), 1);
        this.channel.write(new FrostbitePacket<>(0, CommandType.CUSTOM_CHALLENGE, outStream, packet.getAddress()));
    }

    private void onCustomChallengeResponse(FrostbitePacket<InBitStream> packet) {
        // TODO: 4/3/2022 Verify challenge response. Requires proper octet reading/writing in BitStream impls.
        OutBitStream outStream = OutBitStream.init();
        outStream.write(this.challengeNumber, 32);
        this.channel.write(new FrostbitePacket<>(0, CommandType.CHALLENGE_RESPONSE, outStream, packet.getAddress()));
    }

    private void onConnect(FrostbitePacket<InBitStream> packet) {
        log.info("Received connect request");
        InBitStream data = packet.getData();
        long clientNonce = data.read(32);
        long challengeNumber = data.read(32);

        if (challengeNumber != this.challengeNumber) {
            log.warn("Challenge number mismatch (expected {}, got {})", this.challengeNumber, challengeNumber);
            //return;
        }

        this.sendBusy(packet.getAddress());

        short infoSize = (short) data.read(16);
        byte[] info = data.readOctets(infoSize);
        log.info("Nonce: {}, challenge number: {}, info: {}", clientNonce, challengeNumber, infoSize);

        this.connectionManager.onConnectionRequest(this, packet.getAddress(), clientNonce, info);
    }

    public void acceptConnection(InetSocketAddress address, long validLocalPlayersMask, long clientNonce) {
        ServerConnection connection = new ServerConnection(this, address, this.connections.size() + 1);

        connection.setValidLocalPlayersMask(validLocalPlayersMask);
        connection.setClientNonce(clientNonce);

        short packetChannel = (short) ThreadLocalRandom.current().nextInt(10000);
        connection.setChannelId(packetChannel);

        log.info("Accepting connection from {}, using packet channel {}", clientNonce, packetChannel);

        connection.init();
        connection.sendAccept();

        this.connections.add(connection);
    }

    private void sendBusy(InetSocketAddress address) {
        this.channel.write(new FrostbitePacket<OutBitStream>(0, CommandType.BUSY, null, address));
    }

    private void sendReject(InetSocketAddress address, long clientNonce, DisconnectReason reason) {
        log.info("Rejecting connection {} with reason {}", clientNonce, reason);
        OutBitStream stream = OutBitStream.init();
        stream.write(clientNonce, 32);
        stream.write(reason.ordinal(), 32);
        this.channel.write(new FrostbitePacket<>(0, CommandType.REJECT, stream, address));
    }

    public void tick() {
        for (ServerConnection connection : new HashSet<>(this.connections)) {
            if (System.currentTimeMillis() - connection.getLastReceiveTime() > 10000) {
                log.info("Connection {} timed out", connection.getChannelId());
                connection.unsafeDisconnect(DisconnectReason.TIMED_OUT, "Connection timed out");
                continue;
            }
            connection.tick();
        }
    }

}
