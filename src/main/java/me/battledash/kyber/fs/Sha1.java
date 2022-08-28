package me.battledash.kyber.fs;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Sha1 {

    public static final Sha1 ZERO = new Sha1(new byte[20]);

    private final long a;
    private final long b;
    private final long c;
    private final long d;
    private final long e;

    public Sha1(byte[] bytes) {
        if (bytes.length < 20) {
            throw new IllegalArgumentException("bytes must be at least 20 bytes long");
        }

        a = bytes[0] | bytes[1] << 8 | bytes[2] << 16 | bytes[3] << 24;
        b = bytes[4] | bytes[4 + 1] << 8 | bytes[4 + 2] << 16 | bytes[4 + 3] << 24;
        c = bytes[(2 * 4)] | bytes[(2 * 4) + 1] << 8 | bytes[(2 * 4) + 2] << 16 | bytes[(2 * 4) + 3] << 24;
        d = bytes[(3 * 4)] | bytes[(3 * 4) + 1] << 8 | bytes[(3 * 4) + 2] << 16 | bytes[(3 * 4) + 3] << 24;
        e = bytes[(4 * 4)] | bytes[(4 * 4) + 1] << 8 | bytes[(4 * 4) + 2] << 16 | bytes[(4 * 4) + 3] << 24;
    }

    @Override
    public String toString() {
        return String.format("%08x%08x%08x%08x%08x", a, b, c, d, e);
    }

}
