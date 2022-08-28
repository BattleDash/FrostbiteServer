package me.battledash.kyber.streams;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import me.battledash.kyber.util.MathUtil;

public class BitStreamWrite {

    @Getter
    private final OutBitStream stream;

    public BitStreamWrite() {
        this.stream = new OutBitStream();
    }

    public BitStreamRead convertToRead(int bits) {
        BitStreamRead read = new BitStreamRead();
        read.initRead(this.stream.getBuffer(), bits);
        return read;
    }

    public BitStreamRead convertToRead() {
        return this.convertToRead(this.tell());
    }

    public void initWrite(ByteBuf buffer, int byteCount) {
        this.stream.initBytes(buffer, byteCount);
    }

    public void seek(int bitPos) {
        this.stream.seek(bitPos);
    }

    public boolean isOverflown() {
        return this.stream.isOverflow();
    }

    public void writeUnsigned(long value, int numBits) {
        this.stream.write(value, numBits);
    }

    public void writeUnsigned64(long value, int numBits) {
        this.writeUnsigned(value, Math.abs(Math.min(numBits, 32)));
        this.writeUnsigned(value >> 32L, Math.max(0, numBits - 32));
    }

    public void writeBits(long value, int numBits) {
        Preconditions.checkArgument(numBits <= 32, "numBits must be <= 32");
        this.writeUnsigned(value, numBits);
    }

    public void writeUnsignedLimit(long value, long lowerLimit, long upperLimit) {
        long numBits = MathUtil.getBitsNeeded64(upperLimit - lowerLimit);
        this.stream.write(value - lowerLimit, (int) numBits);
    }

    public void writeStream(BitStreamRead stream, int bits) {
        this.stream.writeStream(stream.getStream(), bits);
    }

    public void writeStream(InBitStream stream, int bits) {
        this.stream.writeStream(stream, bits);
    }

    public boolean writeBool(boolean value) {
        this.writeUnsigned(value ? 1 : 0, 1);
        return value;
    }

    public void writeString(String string) {
        int size = Math.abs(Math.min(string.length(), 1023));
        this.writeUnsigned(size, 10);
        this.stream.writeOctets(Unpooled.wrappedBuffer(string.getBytes()), size);
    }

    public void writeUnsignedFloat(float value) {
        this.writeUnsigned(Float.floatToRawIntBits(value), 31);
    }

    public void flush() {
        this.stream.flush();
    }

    public long getRemainingBitCount() {
        return this.stream.getRemainingBitCount();
    }

    public long getBitCount() {
        return this.tell();
    }

    public long getTotalBufferBitCount() {
        return this.stream.getBufferBitSize();
    }

    public int tell() {
        return this.stream.getStreamBitWritePosition();
    }

}
