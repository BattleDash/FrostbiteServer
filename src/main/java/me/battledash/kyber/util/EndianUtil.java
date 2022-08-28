package me.battledash.kyber.util;

public class EndianUtil {

    public enum Endianness {
        BIG,
        LITTLE
    }

    public static <T> T getEndianSwap(T value) {
        if (value instanceof Integer) {
            return (T) Integer.valueOf(getEndianSwap((Integer) value));
        } else if (value instanceof Long) {
            return (T) Long.valueOf(getEndianSwap((Long) value));
        } else if (value instanceof Short) {
            return (T) Short.valueOf(getEndianSwap((Short) value));
        } else if (value instanceof Byte) {
            return (T) Byte.valueOf(getEndianSwap((Byte) value));
        } else {
            return value;
        }
    }

}
