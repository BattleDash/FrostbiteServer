package me.battledash.kyber.util;

public class Fnv1 {

    public static long hashWithSeed(byte[] data, long hash) {
        for (byte datum : data) {
            hash = hash * 33 ^ datum;
        }
        return hash;
    }

    public static long hashString(String str) {
        long l = Fnv1.hashWithSeed(str.toLowerCase().getBytes(), 5381L);
        return l & 0xFFFFFFFFL;
    }

    public static long hashStringNoLowercase(String str) {
        long l = Fnv1.hashWithSeed(str.getBytes(), 5381L);
        return l & 0xFFFFFFFFL;
    }

}
