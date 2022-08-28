package me.battledash.kyber.streams;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.util.MathUtil;

@Slf4j
public class BitStreamRead {

    @Getter
    private final InBitStream stream;

    public BitStreamRead() {
        this.stream = new InBitStream();
    }

    public void initRead(ByteBuf buffer, int bitCount) {
        this.stream.initBits(buffer, bitCount);
    }

    public boolean isOverflown() {
        return this.stream.isOverflow();
    }

    public void seek(int bitPos) {
        this.stream.seek(bitPos);
    }

    public long readUnsigned(long numBits) {
        return this.stream.read(numBits);
    }

    public long readBits(int numBits) {
        Preconditions.checkArgument(numBits <= 32, "numBits must be <= 32");
        return this.readUnsigned(numBits);
    }

    public long readUnsigned64(long numBits) {
        long value = this.readUnsigned(Math.abs(Math.min(numBits, 32)));
        if (numBits > 32) {
            long highBits = this.readUnsigned(numBits - 32);
            highBits <<= 32;
            value |= highBits;
        }
        return value;
    }

    public long readUnsignedLimit(long lowerLimit, long upperLimit) {
        long numBits = MathUtil.getBitsNeeded64(upperLimit - lowerLimit);
        return this.stream.read(numBits) + lowerLimit;
    }

    public boolean readBool() {
        return (this.stream.read(1) & 1) > 0;
    }

    public float readFloat() {
        return this.readUnsigned(32);
    }

    public String readString() {
        int len = (int) this.readUnsigned(10);
        return new String(this.stream.readOctets(len));
    }

    public float readUnsignedFloat() {
        return (float) this.stream.read(31);
    }

    public void readBitset(int numBits) {
        int v12 = 2;
        do {
            this.readUnsigned(32);
            v12--;
        } while (v12 > 0);
        this.readUnsigned(11);
    }

    public long readSign() {
        return this.readBits(1) << 31;
    }

    public float readUnsignedUnitFloat(long numBits) {
        float f = 0.0f;
        int i = 0;

        if (numBits >= 24) {
            if (this.readBits(1) > 0) {
                return 0.0f;
            }

            i = (int) this.readBits(23);
            i |= MathUtil.selGtz(i, 0x3f800000, 0x40000000);
            //f -= 1.0f;
            return f;
        } else {
            f = this.readBits((int) numBits);
            return f;
        }
    }

    public float readSignedUnitFloat(long numBits) {
        long sign = this.readSign();
        float value = this.readUnsignedUnitFloat(numBits - 1);
        return value;
    }

    public float readUnsignedFloatScale(long numBits, float scale) {
        return this.readUnsignedUnitFloat(numBits) * scale;
    }

    public float readSignedFloatScale(long numBits, float scale) {
        return this.readSignedUnitFloat(numBits) * scale;
    }

    public void readVector() {
        this.readFloat();
        this.readFloat();
        this.readFloat();
    }

    public boolean skipBits(int numBits) {
        this.stream.skip(numBits);

        return !this.stream.isOverflow();
    }

    public long getRemainingBitCount() {
        return this.stream.getRemainingBitCount();
    }

    public int tell() {
        return this.stream.getStreamBitReadPosition();
    }

}
