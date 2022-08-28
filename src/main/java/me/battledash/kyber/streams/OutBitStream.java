package me.battledash.kyber.streams;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.util.BufferUtil;

@Slf4j
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OutBitStream {

    private static final ByteBufAllocator ALLOCATOR = PooledByteBufAllocator.DEFAULT;

    public static int DEFAULT_BUFFER_SIZE = 3000;

    public static OutBitStream init(int size) {
        OutBitStream stream = new OutBitStream();
        //stream.initBytes(OutBitStream.ALLOCATOR.buffer(size, size), size);
        ByteBuf buffer = Unpooled.buffer(size);
        for (int i = 0; i < size; i++) {
            buffer.writeByte(0xcd);
        }
        stream.initBytes(buffer, size);
        return stream;
    }

    public static OutBitStream init() {
        return OutBitStream.init(1024);
    }

    private ByteBuf buffer;
    private int bufferBitSize;
    private int bufferEnd;
    private long streamChunk;
    private int streamChunkWritePtr;
    private int streamBitWritePosition;
    private boolean overflow;

    public void initBytes(ByteBuf buffer, int byteCount) {
        if ((byteCount & 3) != 0) {
            //throw new IllegalArgumentException("Bit count must be 4-byte aligned");
        }

        this.buffer = buffer.retainedDuplicate();
        this.bufferEnd = byteCount;
        this.bufferBitSize = byteCount * 8;
        this.streamChunkWritePtr = 0;
        this.streamChunk = this.buffer.getUnsignedIntLE(this.streamChunkWritePtr);
        this.seek(0);
    }

    public void seek(int startBit) {
        if (startBit < 0 || startBit > this.bufferBitSize) {
            throw new IllegalArgumentException("Start bit must be between 0 and buffer size");
        }

        this.streamBitWritePosition = startBit;

        if (this.streamChunkWritePtr < this.bufferEnd) {
            BufferUtil.setUnsignedIntLE(this.buffer, this.streamChunkWritePtr, this.streamChunk);
        }
        this.streamChunkWritePtr = (this.streamBitWritePosition >> 5) * 4;
        if (this.streamChunkWritePtr < this.bufferEnd) {
            this.streamChunk = this.buffer.getUnsignedIntLE(this.streamChunkWritePtr);
        }
    }

    public InBitStream convertToRead() {
        InBitStream read = new InBitStream();
        read.initBits(this.buffer, this.streamBitWritePosition, false);
        return read;
    }

    public long getRemainingBitCount() {
        return this.getBufferBitSize() - this.getStreamBitWritePosition();
    }

    public int getOctetCount() {
        return (this.getStreamBitWritePosition() + 7) >> 3;
    }

    public boolean flush() {
        if (this.streamChunkWritePtr < this.bufferEnd) {
            BufferUtil.setUnsignedIntLE(this.buffer, this.streamChunkWritePtr, this.streamChunk);
            return true;
        }
        return false;
    }

    public void writeStream(InBitStream stream) {
        this.writeStream(stream, (int) stream.getRemainingBitCount());
    }

    public void writeStream(InBitStream stream, int bitCount) {
        int streamBitWritePosition = this.streamBitWritePosition;
        this.streamBitWritePosition += bitCount;

        if (this.streamBitWritePosition > this.bufferBitSize) {
            log.warn("Stream overflow");
            this.overflow = true;
            return;
        }

        ByteBuf readBuffer = stream.getBuffer();
        long readBitPos = stream.getStreamBitReadPosition();

        long offsetInUint = readBitPos & 31L;
        if (offsetInUint > 0) {
            long bitsToRead = Math.min(bitCount, 32L - offsetInUint);
            long value = stream.read(bitsToRead);
            this.internalWrite(streamBitWritePosition, value, (int) bitsToRead);
            streamBitWritePosition += bitsToRead;
            readBitPos += bitsToRead;
            bitCount -= bitsToRead;
        }

        long uintCount = bitCount >> 5L;
        if (uintCount > 0) {
            ByteBuf data = Unpooled.buffer(OutBitStream.DEFAULT_BUFFER_SIZE, OutBitStream.DEFAULT_BUFFER_SIZE);
            int i = (int) (readBitPos >> 3L);
            readBuffer.getBytes(i, data, readBuffer.capacity() - i);
            this.internalWriteOctets(streamBitWritePosition, data, (int) uintCount << 2);
            long bitsWritten = uintCount << 5L;
            bitCount -= bitsWritten;
            streamBitWritePosition += bitsWritten;
            stream.skip((int) bitsWritten);
        }

        if (bitCount > 0) {
            long value = stream.read(bitCount);
            this.internalWrite(streamBitWritePosition, value, bitCount);
        }
    }

    public void writeString(String string) {
        this.write(string.length(), 16);
        this.writeOctets(Unpooled.wrappedBuffer(string.getBytes()), string.length());
    }

    public void write(long data, int bitCount) {
        Preconditions.checkArgument(bitCount > 0, "bitCount must be > 0 (%s)", bitCount);

        int streamBitWritePosition = this.streamBitWritePosition;
        this.streamBitWritePosition += bitCount;
        this.internalWrite(streamBitWritePosition, data, bitCount);
    }

    private void internalWrite(int streamBitWritePosition, long data, int bitCount) {
        long bitsConsumedInChunk = streamBitWritePosition & 31L;
        long bitsLeftInChunk = 32 - bitsConsumedInChunk;
        long extraBits = bitCount - bitsLeftInChunk;

        if (extraBits < 0) {
            int mask = ((1 << bitCount) - 1) << bitsConsumedInChunk;
            this.streamChunk = ((data << bitsConsumedInChunk) & mask) | (this.streamChunk & ~mask);
        } else {
            long mask1 = (1L << bitsConsumedInChunk) - 1L;
            long streamChunk = (data << bitsConsumedInChunk) | (this.streamChunk & mask1);

            BufferUtil.setUnsignedIntLE(this.buffer, this.streamChunkWritePtr, streamChunk);
            this.streamChunkWritePtr += 4;
            if (this.streamChunkWritePtr < this.bufferEnd) {
                streamChunk = this.buffer.getUnsignedIntLE(this.streamChunkWritePtr);
            }

            long mask2 = (1L << extraBits) - 1L;
            this.streamChunk = (streamChunk & ~mask2) | ((data >> bitsLeftInChunk) & mask2);
        }
    }

    // Be careful when using this, it bypasses bit-level writing
    public void append(ByteBuf data, int count) {
        for (int i = 0; i < count; i++) {
            this.write(data.getByte(i), 8);
        }
    }

    public void writeOctets(ByteBuf data, int count) {
        int streamBitWritePosition = this.streamBitWritePosition;
        int bitCount = count << 3;
        this.streamBitWritePosition += bitCount;
        this.internalWriteOctets(streamBitWritePosition, data, count);
    }

    private void internalWriteOctets(int streamBitWritePosition, ByteBuf data, int count) {
        long bitsConsumedInChunk = streamBitWritePosition & 31;
        long extraOctets = count & 3;

        if (bitsConsumedInChunk == 0) {
            int streamChunkWritePtr = this.streamChunkWritePtr;
            long copyCount = count - extraOctets;
            this.buffer.setBytes(streamChunkWritePtr, data, Math.toIntExact(copyCount));
            streamChunkWritePtr += (copyCount >> 2) * 4;

            long writeStreamChunk = 0;
            if (streamChunkWritePtr < this.bufferEnd) {
                writeStreamChunk = this.getStreamChunk(streamChunkWritePtr, true);
            }

            long rest = switch ((int) extraOctets) {
                case 0 -> writeStreamChunk;
                case 1 -> (writeStreamChunk & 0xFFFFFF00) | data.getUnsignedInt((int) (copyCount + 0));
                case 2 -> (writeStreamChunk & 0xFFFF0000) | (data.getUnsignedInt((int) (copyCount + 1)) << 8) | data.getUnsignedInt((int) (copyCount + 0));
                default -> (writeStreamChunk & 0xFF000000) | (data.getUnsignedInt((int) (copyCount + 2)) << 16) | (data.getUnsignedInt((int) (copyCount + 1)) << 8) | data.getUnsignedInt((int) (copyCount + 0));
            };

            this.streamChunkWritePtr = streamChunkWritePtr;
            this.streamChunk = rest;
        } else {
            long bitsLeftInChunk = 32L - bitsConsumedInChunk;
            long consumedMask = (1L << bitsConsumedInChunk) - 1L;

            long streamChunk = this.streamChunk;

            int streamChunkWritePtr = this.streamChunkWritePtr;
            int intCount = count >> 2;
            int readPos = 0;

            for (int endReadPos = readPos + intCount; readPos != endReadPos; ++readPos) {
                long value = data.getUnsignedIntLE(readPos * 4);
                streamChunk = (value << bitsConsumedInChunk) | (streamChunk & consumedMask);
                BufferUtil.setUnsignedIntLE(this.buffer, streamChunkWritePtr, streamChunk);
                streamChunkWritePtr += 4;
                streamChunk = value >> bitsLeftInChunk;
            }

            if (streamChunkWritePtr < this.bufferEnd) {
                streamChunk = (this.buffer.getUnsignedIntLE(streamChunkWritePtr) & ~consumedMask) | (streamChunk & consumedMask);
            }

            this.streamChunkWritePtr = streamChunkWritePtr;
            this.streamChunk = streamChunk;

            if (extraOctets > 0) {
                long bitCount = extraOctets << 3;

                int index = readPos * 4;
                long value = switch ((int) extraOctets) {
                    case 1 -> BufferUtil.readIntPart(data, index);
                    case 2 -> (BufferUtil.readIntPart(data, index + 1) << 8) | BufferUtil.readIntPart(data, index);
                    default -> (BufferUtil.readIntPart(data, index + 2) << 16) | (BufferUtil.readIntPart(data, index + 1) << 8) | BufferUtil.readIntPart(data, index);
                };

                this.internalWrite(streamBitWritePosition, value, (int) bitCount);
            }
        }
    }

    private long getStreamChunk(int streamChunkWritePtr, boolean bypass) {
        long streamChunk;
        byte[] buffer = {0,0,0,0};
        for (int i = 0; i < 4; i++) {
            if (!bypass && streamChunkWritePtr >= this.getOctetCount()) {
                break;
            }
            buffer[i] = this.buffer.getByte(streamChunkWritePtr++);
        }
        streamChunk = ((long) buffer[0] & 0xFF);
        streamChunk |= ((long) buffer[1] & 0xFF) << 8;
        streamChunk |= ((long) buffer[2] & 0xFF) << 16;
        streamChunk |= ((long) buffer[3] & 0xFF) << 24;

        return streamChunk;
    }

    private long getStreamChunk(int streamChunkWritePtr) {
        return this.getStreamChunk(streamChunkWritePtr, false);
    }

    private long getStreamChunk() {
        return this.getStreamChunk(this.streamChunkWritePtr);
    }

    public void debugPrint(String name) {
        log.info(name + ": " + ByteBufUtil.hexDump(this.buffer.copy(0, this.getOctetCount())));
    }

    public void debugPrint() {
        this.debugPrint("Buffer");
    }

}
