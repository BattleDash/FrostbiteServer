package me.battledash.kyber.util;

public class MathUtil {

    public static int selGtz(int condition, int a, int b) {
        int maskNotZero = (-condition | condition) >> (8 * 4 - 1);
        int mask = -(condition | 1) >> (8 * 4 - 1);
        return b + ((a - b) & mask & maskNotZero);
    }

    public static long getBitsNeeded32(int value) {
        return 32 - Integer.numberOfLeadingZeros(value);
    }

    public static long getBitsNeeded64(long value) {
        return 64 - Long.numberOfLeadingZeros(value);
    }

}
