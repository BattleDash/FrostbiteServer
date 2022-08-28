package me.battledash.kyber.network.stream;

import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.streams.BitStreamRollback;
import me.battledash.kyber.streams.BitStreamWrite;

@Slf4j
public class StreamManagerFrame extends StreamManager {

    private static final int FRAME_WINDOW_BITS = 16;
    private static final int FRAME_WINDOW_SIZE = 1 << StreamManagerFrame.FRAME_WINDOW_BITS;
    private static final int FRAME_WINDOW_MASK = StreamManagerFrame.FRAME_WINDOW_SIZE - 1;

    private static final int SEQUENCE_SIZE_BITS = 15;
    private static final int MAX_SEQUENCE_COUNT = 64;
    private static final int FIRST_SEQUENCE_VALUE = 0;
    private static final int MAX_BUFFER_SIZE = 1600;

    private final BitStreamWrite incomingBitStream = new BitStreamWrite();

    @Setter
    private StreamManager streamManager;

    private long lastReceivedFrameNumber = ~0;
    private long lastReceivedSequenceNumber = ~0;
    private long nextExpectedSequenceNumber = ~0;
    private boolean incomingFrameIsBroken;

    private BitStreamWrite outgoingBitStream;
    private int outgoingBitStreamSeek;

    @Getter
    private final String name = "Frame";

    private long frameNumberToSend = 0;
    private long sequenceNumberToSend = 0;

    public void init(StreamManager manager) {
        this.streamManager = manager;
    }

    private boolean isValidPacket(long frameNumber, long sequenceNumber) {
        if (frameNumber != this.lastReceivedFrameNumber && sequenceNumber == 0) {
            return true;
        }

        return frameNumber == this.lastReceivedFrameNumber && sequenceNumber == this.nextExpectedSequenceNumber && !this.incomingFrameIsBroken;
    }

    @Override
    public TransmitResult transmitPacket(BitStreamWrite stream) {
        BitStreamRollback rollback = new BitStreamRollback(stream);

        stream.writeUnsigned(this.frameNumberToSend, StreamManagerFrame.FRAME_WINDOW_BITS);
        stream.writeUnsignedLimit(this.sequenceNumberToSend, 0, StreamManagerFrame.MAX_SEQUENCE_COUNT - 1);
        int streamPosition = stream.tell();
        stream.writeUnsigned(0, StreamManagerFrame.SEQUENCE_SIZE_BITS);
        stream.writeBool(false);

        if (stream.isOverflown()) {
            return TransmitResult.NOTHING_TO_SEND;
        }

        int expectedSize = 0;

        if (this.outgoingBitStreamSeek != 0) {
            this.outgoingBitStream.seek(this.outgoingBitStreamSeek);
        } else {
            this.outgoingBitStream = new BitStreamWrite();
            this.outgoingBitStream.initWrite(Unpooled.buffer(StreamManagerFrame.MAX_BUFFER_SIZE, StreamManagerFrame.MAX_BUFFER_SIZE),
                    StreamManagerFrame.MAX_BUFFER_SIZE);
            this.streamManager.transmitPacket(this.outgoingBitStream);
            this.outgoingBitStream.flush();
            expectedSize = this.outgoingBitStream.tell();
        }

        long totalBitCountLeft = this.outgoingBitStream.getBitCount() - this.outgoingBitStreamSeek;
        long bitCountLeftInPacket = stream.getRemainingBitCount();

        int bitCountToCopy = Math.toIntExact(Math.min(totalBitCountLeft, bitCountLeftInPacket));

        this.outgoingBitStream.seek(this.outgoingBitStreamSeek);
        stream.writeStream(this.outgoingBitStream.convertToRead(bitCountToCopy), bitCountToCopy);
        int posAfterWrite = stream.tell();
        this.outgoingBitStreamSeek += bitCountToCopy;
        stream.seek(streamPosition);
        stream.writeUnsigned(bitCountToCopy, StreamManagerFrame.SEQUENCE_SIZE_BITS);
        boolean lastSequence = this.outgoingBitStreamSeek == expectedSize;

        stream.writeBool(lastSequence);
        stream.seek(posAfterWrite);
        if (!lastSequence) {
            this.sequenceNumberToSend++;
            return TransmitResult.INCOMPLETE_CONTINUE;
        }

        this.prepareNextFrame();

        return TransmitResult.WHOLE;
    }

    private void prepareNextFrame() {
        this.frameNumberToSend = (this.frameNumberToSend + 1) & StreamManagerFrame.FRAME_WINDOW_MASK;
        this.sequenceNumberToSend = StreamManagerFrame.FIRST_SEQUENCE_VALUE;
        this.outgoingBitStreamSeek = 0;
    }

    @Override
    public boolean processReceivedPacket(BitStreamRead stream) {
        long frameNumber = stream.readUnsigned(StreamManagerFrame.FRAME_WINDOW_BITS);
        long sequenceNumber = stream.readUnsignedLimit(0, StreamManagerFrame.MAX_SEQUENCE_COUNT - 1);
        long sequenceBitCount = stream.readUnsigned(StreamManagerFrame.SEQUENCE_SIZE_BITS);
        boolean lastSequence = stream.readBool();

        log.debug("Received frame#{} seq#{} bits: {} lastSequence: {}",
                frameNumber, sequenceNumber, sequenceBitCount, lastSequence);

        if (this.lastReceivedFrameNumber == ~0 && frameNumber > 1) {
            log.debug("Received invalid packet. (frame#{} seq#{} bits: {} lastSequence: {})",
                    frameNumber, sequenceNumber, sequenceBitCount, lastSequence);
            frameNumber = 0;
            sequenceNumber = 0;
            //return true;
        }

        this.incomingFrameIsBroken = !this.isValidPacket(frameNumber, sequenceNumber);
        if (this.incomingFrameIsBroken) {
            log.warn("Received invalid packet. Frame {} (last was {}) Sequence {} (last was {})",
                    frameNumber, this.lastReceivedFrameNumber,
                    sequenceNumber, this.lastReceivedSequenceNumber);
            stream.skipBits((int) sequenceBitCount);
            this.lastReceivedFrameNumber = frameNumber;
            this.lastReceivedSequenceNumber = sequenceNumber;
            this.nextExpectedSequenceNumber = StreamManagerFrame.FIRST_SEQUENCE_VALUE;
            return true;
        }

        if (sequenceNumber == StreamManagerFrame.FIRST_SEQUENCE_VALUE) {
            this.incomingBitStream.initWrite(Unpooled.buffer(StreamManagerFrame.MAX_BUFFER_SIZE, StreamManagerFrame.MAX_BUFFER_SIZE),
                    StreamManagerFrame.MAX_BUFFER_SIZE);
        }

        if (sequenceNumber > this.incomingBitStream.getRemainingBitCount()) {
            if (sequenceBitCount > this.incomingBitStream.getRemainingBitCount()) {
                log.error("Stream overflow. Expected at most {} bits but got {}. Probably buffer size mismatch" +
                        " on client and server. Buffer size in bytes is {} and current stream position is {}",
                        this.incomingBitStream.getRemainingBitCount(), sequenceBitCount,
                        1600, this.incomingBitStream.tell());
            }
            stream.skipBits((int) sequenceBitCount);
            this.lastReceivedFrameNumber = frameNumber;
            this.lastReceivedSequenceNumber = sequenceNumber;
            this.nextExpectedSequenceNumber = StreamManagerFrame.FIRST_SEQUENCE_VALUE;
            this.incomingFrameIsBroken = true;
            return false;
        }

        this.incomingBitStream.writeStream(stream, (int) sequenceBitCount);

        boolean ok = true;

        if (lastSequence) {
            this.incomingBitStream.flush();
            ok = this.streamManager.processReceivedPacket(this.incomingBitStream.convertToRead());
        }

        this.lastReceivedFrameNumber = frameNumber;
        this.lastReceivedSequenceNumber = sequenceNumber;
        this.nextExpectedSequenceNumber = sequenceNumber + 1;
        return ok;
    }

}
