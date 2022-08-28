package me.battledash.kyber.network.stream;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.Tickable;
import me.battledash.kyber.server.GameContext;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.streams.BitStreamWrite;
import me.battledash.kyber.streams.InBitStream;
import me.battledash.kyber.streams.OutBitStream;
import me.battledash.kyber.network.PacketConnection;
import me.battledash.kyber.network.packet.HandshakingState;
import me.battledash.kyber.network.packet.PacketType;
import me.battledash.kyber.server.ServerGameContext;

@Slf4j
@Getter
public class StreamControl implements Tickable {

    private static final int NETWORK_PROCESSING_DELAY_BITS = 8;
    private static final int NETWORK_PROCESSING_DELAY_MAX = (1 << StreamControl.NETWORK_PROCESSING_DELAY_BITS) - 1;

    private final BitStreamRead bitStream = new BitStreamRead();

    private final StreamManagerFrame frameManager = new StreamManagerFrame();
    private final StreamManagerEngine streamManager = new StreamManagerEngine();

    @Setter
    private StreamManager manager;

    private final PacketConnection connection;
    private final boolean largePacketSupport;

    private boolean doSend;
    private float packetFrequency = 1.f/200.f;
    private float timeToNextSend = 0f;

    public StreamControl(PacketConnection connection, boolean largePacketSupport) {
        this.connection = connection;
        this.largePacketSupport = largePacketSupport;
        this.frameManager.setStreamManager(this.streamManager);
    }

    public void internalTransmit(BitStreamWrite stream) {
        stream.writeUnsigned(StreamControl.NETWORK_PROCESSING_DELAY_MAX, StreamControl.NETWORK_PROCESSING_DELAY_BITS);

        boolean sendStats = false;
        if (stream.writeBool(sendStats)) {
            stream.writeUnsigned(0, 32);
            stream.writeUnsignedFloat(0);
            stream.writeUnsignedFloat(0);
        }
    }

    public void internalReceive(BitStreamRead stream) {
        stream.readUnsigned(StreamControl.NETWORK_PROCESSING_DELAY_BITS);
        if (stream.readBool()) {
            long remoteIncomingRate = stream.readUnsigned(32);
            float remoteIncomingFrequency = stream.readUnsignedFloat();
            float remoteOutgoingFrequency = stream.readUnsignedFloat();
            log.debug("RemoteIncRate={}, RemoteIncFreq={}, RemoteOutFreq={}",
                    remoteIncomingRate, remoteIncomingFrequency, remoteOutgoingFrequency);
        }
    }

    public void internalReceiveBitStream(InBitStream stream) {
        this.bitStream.initRead(stream.getBuffer(), stream.getBufferBitSize());
        this.bitStream.seek(stream.getStreamBitReadPosition());
        this.internalReceive(this.bitStream);
        //this.bitStream.readUnsigned(32);
        byte[] bytes = new byte[stream.getBufferBitSize() / 8];
        //stream.getBuffer().getBytes(this.bitStream.getStream().getStreamBitReadPosition() / 8, bytes);
        log.debug("Received packet {}, {}, {}, {}", this.bitStream.getStream().getStreamChunkReadPtr(),
                this.bitStream.getStream().getStreamBitReadPosition(), this.bitStream.getStream().getStreamChunk(),
                ByteBufUtil.hexDump(Unpooled.wrappedBuffer(bytes)));
        this.manager.processReceivedPacket(this.bitStream);
    }

    @Override
    public void tick() {
        this.timeToNextSend -= ServerGameContext.context().getServer().getSecondsPerTick();
        if (this.timeToNextSend <= 0f) {
            this.doSend = true;
            this.timeToNextSend = (this.timeToNextSend % this.packetFrequency) + this.packetFrequency;
        }

        if (!this.doSend || this.manager == null || this.connection.getState() != HandshakingState.PLAY || GameContext.DISABLE_PACKET_TRANSFER) {
            return;
        }

        TransmitResult result;
        BitStreamWrite stream = new BitStreamWrite();
        do {
            stream.initWrite(Unpooled.buffer(1024, 1024), 1024);

            this.internalTransmit(stream);

            result = this.manager.transmitPacket(stream);

            this.connection.sendPacket(PacketType.DATA, stream.getStream());
        } while (result == TransmitResult.INCOMPLETE_CONTINUE && this.largePacketSupport);

        this.doSend = false;
    }

}
