package me.battledash.kyber.streams;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class InBitStream {

    private ByteBuf buffer;
    private int bufferBitSize;
    private int bufferEnd;
    private long streamChunk;
    private int streamChunkReadPtr;
    private int streamBitReadPosition;
    private boolean overflow;

    public void initBits(ByteBuf buffer, int bitCount, boolean copy) {
        if ((buffer.readableBytes() & 3) == 0) {
            //throw new IllegalArgumentException("Bit count must be 4-byte aligned");
        }

        this.buffer = copy ? buffer.retainedDuplicate() : buffer;
        this.buffer.resetReaderIndex();
        this.bufferBitSize = bitCount;
        this.bufferEnd = ((bitCount + 31) & ~31) >> 3;
        this.seek(0);
    }

    public void initBits(ByteBuf buffer, int bitCount) {
        this.initBits(buffer, bitCount, true);
    }

    public void seek(int startBit) {
        if (startBit < 0 || startBit > this.bufferBitSize) {
            throw new IllegalArgumentException("Start bit must be between 0 and buffer size (startBit: " +
                    startBit + ", bufferSize: " + this.bufferBitSize + ")");
        }

        if (startBit >= this.bufferBitSize) {
            this.streamBitReadPosition = this.bufferBitSize;
            return;
        }

        this.streamBitReadPosition = startBit;
        this.streamChunkReadPtr = (this.streamBitReadPosition >> 5) * 4;
        this.streamChunk = this.readStreamChunk(true);
    }

    public OutBitStream convertToWrite() {
        OutBitStream init = OutBitStream.init(this.getOctetCount());
        init.initBytes(this.buffer, this.getOctetCount());
        return init;
    }

    public void skip(int bitCount) {
        this.seek(this.getStreamBitReadPosition() + bitCount);
    }

    public long read(long bitCount) {
        long bitsConsumedInChunk = this.streamBitReadPosition & 31L;
        long bitsLeftInChunk = 32 - bitsConsumedInChunk;

        this.streamBitReadPosition += bitCount;
        if (this.streamBitReadPosition > this.bufferBitSize) {
            log.warn("Cannot read past end of buffer (read " + this.streamBitReadPosition + "/" + this.bufferBitSize + ")");
            this.overflow = true;
            return 0;
        }

        long data;

        if (bitCount < bitsLeftInChunk) {
            long mask = (1L << bitCount) - 1L;
            log.debug("mask: {}", mask);
            data = (this.streamChunk >> bitsConsumedInChunk) & mask;
            log.debug("streamChunk: {}", this.streamChunk);
        } else {
            long extraBits = bitCount - bitsLeftInChunk;
            log.debug("extraBits: {}", extraBits);

            long streamChunk = this.streamChunk;
            log.debug("streamChunk: {}", streamChunk);
            long loMask = -(1L << bitsConsumedInChunk);
            data = (streamChunk & loMask) >> bitsConsumedInChunk;

            if (this.streamChunkReadPtr < this.bufferEnd) {
                log.debug("streamChunkReadPtr: {}", this.streamChunkReadPtr);
                streamChunk = this.readStreamChunk();
                log.debug("swapped streamChunk: {}", streamChunk);
            }

            long hiMask = (1L << extraBits) - 1L;
            data |= (streamChunk & hiMask) << (bitsLeftInChunk & 31L);

            this.streamChunk = streamChunk;
        }

        return data;
    }

    public byte[] readOctets(int count) {
        long wordCount = count >> 2;
        byte[] data = new byte[count];
        for (int i = 0; i < wordCount; ++i) {
            long word = this.read(32);
            data[4 * i] = (byte) (word & 0xff);
            data[4 * i + 1] = (byte) ((word >> 8) & 0xff);
            data[4 * i + 2] = (byte) ((word >> 16) & 0xff);
            data[4 * i + 3] = (byte) ((word >> 24) & 0xff);
        }
        for (int i = (int) (wordCount << 2); i < count; ++i) {
            data[i] = (byte) this.read(8);
        }
        return data;
    }

    public long getRemainingBitCount() {
        return this.getBufferBitSize() - this.getStreamBitReadPosition();
    }

    public String readString() {
        if (this.getRemainingBitCount() < 16) {
            throw new IllegalStateException("Stream overflow");
        }

        int octets = (int) this.read(16);
        if (octets == 0) {
            return null;
        }

        if ((this.getRemainingBitCount() >> 3L) < octets) {
            throw new IllegalStateException("Stream overflow");
        }

        return new String(this.readOctets(octets));
    }

    private long readStreamChunk(boolean bypass) {
        long streamChunk;
        byte[] buffer = {0,0,0,0};
        for (int i = 0; i < 4; i++) {
            if (this.streamChunkReadPtr >= this.getOctetCount()) {
                if (bypass) {
                    buffer[i] = (byte) 0xcd;
                } else {
                    break;
                }
            }
            try {
                buffer[i] = this.buffer.getByte(this.streamChunkReadPtr++);
            } catch (IndexOutOfBoundsException e) {
                buffer[i] = (byte) 0xcd;
            }
        }
        streamChunk = ((long) buffer[0] & 0xFF);
        streamChunk |= ((long) buffer[1] & 0xFF) << 8;
        streamChunk |= ((long) buffer[2] & 0xFF) << 16;
        streamChunk |= ((long) buffer[3] & 0xFF) << 24;
        return streamChunk;
    }

    private long readStreamChunk() {
        return this.readStreamChunk(false);
    }

    public int getOctetCount() {
        return (this.bufferBitSize + 7) >> 3;
    }

    public String getHexString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.getOctetCount(); i++) {
            sb.append(String.format("%02x", this.buffer.getByte(i)));
        }
        return sb.toString();
    }

    public void debugPrint(String name) {
        log.info(name + ": " + this.getHexString());
    }

    public void debugPrint() {
        this.debugPrint("Buffer");
    }

}
