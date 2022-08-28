package me.battledash.kyber.util;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class BufferUtil {

    public static long readIntPart(ByteBuf buf, int index) {
        int length = Math.min(4, buf.capacity() - index);
        long value = 0;
        for (int i = 0; i < length; i++) {
            value |= (long) (buf.getByte(index + i) & 0xFF) << (i * 8);
        }
        return value;
    }

    public static int calcFletcher16(int length, ByteBuf buf) {
        char[] data = new char[length];
        for (int i = 0; i < length; i++) {
            data[i] = (char) buf.getByte(i);
        }

        int sum1 = 0xff;
        int sum2 = 0xff;
        for (int i = 0; i < length; i++) {
            sum1 = (sum1 + (int) data[i]) % 255;
            sum2 = (sum2 + sum1) % 255;
        }
        return (sum2 << 8) | sum1;
    }

    public static void setUnsignedIntLE(ByteBuf buf, int index, long value) {
        buf.setByte(index, (byte) (value & 0xFF));
        buf.setByte(index + 1, (byte) (value >> 8 & 0xFF));
        buf.setByte(index + 2, (byte) (value >> 16 & 0xFF));
        buf.setByte(index + 3, (byte) (value >> 24 & 0xFF));
    }

    public static void setUnsignedShortLE(ByteBuf buf, int index, int value) {
        buf.setByte(index, (byte) (value & 0xFF));
        buf.setByte(index + 1, (byte) (value >> 8 & 0xFF));
    }

    public static UUID guidToUUID(byte[] guid) {
        ByteBuffer source = ByteBuffer.wrap(guid);
        ByteBuffer target = ByteBuffer.allocate(16)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(source.getInt())
                .putShort(source.getShort())
                .putShort(source.getShort())
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(source.getLong())
                .rewind();
        return new UUID(target.getLong(), target.getLong());
    }

}
