package me.battledash.kyber.fs;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import me.battledash.kyber.util.BufferUtil;
import me.battledash.kyber.util.EndianUtil;

import java.math.BigInteger;
import java.util.UUID;

public class NativeReader {

    @Getter
    protected final ByteBuf buf;

    public NativeReader(ByteBuf buf) {
        this.buf = buf.retainedDuplicate();
    }

    public void pad(int alignment) {
        int readerIndex = this.buf.readerIndex();
        while (readerIndex % alignment != 0) {
            readerIndex++;
        }
        this.buf.readerIndex(readerIndex);
    }

    private byte[] readBuffer(int length) {
        byte[] buffer = new byte[length];
        this.buf.readBytes(buffer);
        return buffer;
    }

    public int readInt(EndianUtil.Endianness endianness) {
        if (endianness == EndianUtil.Endianness.LITTLE) {
            return this.buf.readIntLE();
        } else {
            return this.buf.readInt();
        }
    }

    public int readInt() {
        return this.readInt(EndianUtil.Endianness.LITTLE);
    }

    public int readUShort(EndianUtil.Endianness endianness) {
        if (endianness == EndianUtil.Endianness.LITTLE) {
            return this.buf.readUnsignedShortLE();
        } else {
            return this.buf.readUnsignedShort();
        }
    }

    public int readUShort() {
        return this.readUShort(EndianUtil.Endianness.LITTLE);
    }

    public long readUInt(EndianUtil.Endianness endianness) {
        //byte[] buffer = this.readBuffer(4);
        if (endianness == EndianUtil.Endianness.LITTLE) {
            return this.buf.readUnsignedIntLE();
        } else {
            return this.buf.readUnsignedInt();
        }
    }

    public long readUInt() {
        return this.readUInt(EndianUtil.Endianness.LITTLE);
    }

    public long readLong(EndianUtil.Endianness endianness) {
        //byte[] buffer = this.readBuffer(8);
        if (endianness == EndianUtil.Endianness.LITTLE) {
            return this.buf.readLongLE();
        } else {
            return this.buf.readLong();
        }
    }

    public long readLong() {
        return this.readLong(EndianUtil.Endianness.LITTLE);
    }

    public int read7BitEncodedInt() {
        int result = 0;
        int i = 0;

        while (true) {
            int b = this.buf.readByte();
            result |= (b & 127) << i;

            if (b >> 7 == 0) {
                return result;
            }

            i += 7;
        }
    }

    public long read7BitEncodedLong() {
        long result = 0;
        int i = 0;

        while (true) {
            int b = this.buf.readByte();
            result |= (long) (b & 127) << i;

            if (b >> 7 == 0) {
                return result;
            }

            i += 7;
        }
    }

    public String readNullTerminatedString() {
        StringBuilder sb = new StringBuilder();

        while (true) {
            byte b = this.buf.readByte();

            if (b == 0) {
                break;
            }

            sb.append((char) b);
        }

        return sb.toString();
    }

    public String readSizedString(int size) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0 ; i < size ; i++) {
            char c = (char) this.buf.readByte();
            if (c != 0x00) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public UUID readGuid(EndianUtil.Endianness endianness) {
        byte[] bytes = new byte[16];
        this.buf.readBytes(bytes);
        if (endianness == EndianUtil.Endianness.LITTLE) {
            return BufferUtil.guidToUUID(bytes);
        } else {
            return BufferUtil.guidToUUID(new byte[] {
                    bytes[3], bytes[2], bytes[1], bytes[0], bytes[5], bytes[4], bytes[7], bytes[6],
                    bytes[8], bytes[9], bytes[10], bytes[11], bytes[12], bytes[13], bytes[14], bytes[15]
            });
        }
    }

    public UUID readGuid() {
        return this.readGuid(EndianUtil.Endianness.LITTLE);
    }

    public Sha1 readSha1() {
        byte[] bytes = new byte[20];
        this.buf.readBytes(bytes);
        return new Sha1(bytes);
    }

}
