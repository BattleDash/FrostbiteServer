package me.battledash.kyber.misc;

import java.util.HashMap;
import java.util.Map;

public class StateMask {

    public static final int GHOST_MASK_INIT = 1;
    public static final int GHOST_MASK_DELETE = 1 << 1;
    public static final int RESERVED_STATES = 2;

    private final Map<Long, Long> data = new HashMap<>();

    public long getBits(int bitCount, int bitOffset) {
        int bitsConsumedInWord = bitOffset & 31;
        int bitsLeftInWord = 32 - bitsConsumedInWord;

        long word = bitOffset >> 5;
        long result;

        if (bitCount < bitsLeftInWord) {
            int mask = (1 << bitCount) - 1;
            result = (this.getValue(word) >> bitsConsumedInWord) & mask;
        } else {
            int extraBits = bitCount - bitsLeftInWord;

            int loMask = -(1 << bitsConsumedInWord);
            result = (this.getValue(word) & loMask) >> bitsConsumedInWord;

            word++;

            int hiMask = (1 << extraBits) - 1;
            result |= (this.getValue(word) & hiMask) << (bitsLeftInWord & 31);
        }

        return result;
    }

    public void storeBits(long bits, int bitCount, int bitOffset) {
        int bitsConsumedInChunk = bitOffset & 31;
        int bitsLeftInChunk = 32 - bitsConsumedInChunk;
        int extraBits = bitCount - bitsLeftInChunk;
        long word = bitOffset >> 5;

        if (extraBits < 0) {
            int mask = (1 << bitCount) << bitsConsumedInChunk;
            this.data.put(word, ((bits << bitsConsumedInChunk) & mask) | (this.getValue(word) & ~mask));
        } else {
            int mask1 = (1 << bitsLeftInChunk) - 1;
            this.data.put(word, (bits & mask1) << bitsConsumedInChunk | (this.getValue(word) & mask1));

            word++;

            int mask2 = (1 << extraBits) - 1;
            this.data.put(word, ((bits >> bitsLeftInChunk) & mask2) | (this.getValue(word) & ~mask2));
        }
    }

    public void setBits(long bits, int bitCount, int bitOffset) {
        int bitsConsumedInChunk = bitOffset & 31;
        int bitsLeftInChunk = 32 - bitsConsumedInChunk;
        int extraBits = bitCount - bitsLeftInChunk;
        long word = bitOffset >> 5;

        if (extraBits < 0) {
            int mask = ((1 << bitCount) - 1) << bitsConsumedInChunk;
            this.data.put(word, this.getValue(word) | ((bits << bitsConsumedInChunk) & mask));
        } else {
            this.data.put(word, this.getValue(word) | (bits << bitsConsumedInChunk));

            word++;

            int mask2 = (1 << extraBits) - 1;
            this.data.put(word, this.getValue(word) | (bits >> bitsLeftInChunk) & mask2);
        }
    }

    public boolean testBits(long testBits, int bitCount, int bitOffset) {
        long storedBits = this.getBits(bitCount, bitOffset);
        return (storedBits & testBits) > 0;
    }

    private long getValue(long key) {
        return this.data.getOrDefault(key, 0L);
    }

    public boolean isZero() {
        long mask = 0;
        for (long word : this.data.values()) {
            mask |= word;
        }
        return mask == 0;
    }

}
