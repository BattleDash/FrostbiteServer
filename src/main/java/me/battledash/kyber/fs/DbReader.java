package me.battledash.kyber.fs;

import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.misc.Tuple;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DbReader extends NativeReader {

    public DbReader(ByteBuf buf, boolean deobfuscate) {
        super(buf);
        if (deobfuscate) {
            this.buf.readerIndex(0x22C);
        }
    }

    public DbReader(ByteBuf buf) {
        this(buf, true);
    }

    public <T> Tuple<T, String> readDbObject() {
        String objName = "";
        byte tmp = this.buf.readByte();

        DbType objType = DbType.fromKey(tmp & 0x1F);
        if (objType == DbType.INVALID) {
            return null;
        }

        if ((tmp & 0x80) == 0) {
            objName = this.readNullTerminatedString();
        }

        switch (objType) {
            case LIST -> {
                long size = this.read7BitEncodedLong();
                long offset = this.buf.readerIndex();

                List<Object> values = new ArrayList<>();
                while (this.buf.readerIndex() - offset < size) {
                    Tuple<Object, String> subValue = this.readDbObject();

                    if (subValue == null) {
                        break;
                    }

                    values.add(subValue.getA());
                }
                return (Tuple<T, String>) Tuple.of(new DbObject(values), objName);
            }
            case OBJECT -> {
                long size = this.read7BitEncodedLong();
                long offset = this.buf.readerIndex();

                Map<String, Object> values = new LinkedHashMap<>();
                while (this.buf.readerIndex() - offset < size) {
                    Tuple<DbObject, String> subValue = this.readDbObject();

                    if (subValue == null) {
                        break;
                    }

                    values.put(subValue.getB(), subValue.getA());
                }
                return (Tuple<T, String>) Tuple.of(new DbObject(values), objName);
            }
            case BOOLEAN -> {
                return (Tuple<T, String>) Tuple.of(this.buf.readByte() == 1, objName);
            }
            case STRING -> {
                return (Tuple<T, String>) Tuple.of(this.readSizedString(this.read7BitEncodedInt()), objName);
            }
            case INT -> {
                return (Tuple<T, String>) Tuple.of(this.buf.readUnsignedIntLE(), objName);
            }
            case LONG -> {
                return (Tuple<T, String>) Tuple.of(this.readLong(), objName);
            }
            case FLOAT -> {
                return (Tuple<T, String>) Tuple.of(this.buf.readFloatLE(), objName);
            }
            case DOUBLE -> {
                return (Tuple<T, String>) Tuple.of(this.buf.readDoubleLE(), objName);
            }
            case GUID -> {
                return (Tuple<T, String>) Tuple.of(this.readGuid(), objName);
            }
            case SHA1 -> {
                return (Tuple<T, String>) Tuple.of(this.readSha1(), objName);
            }
            case BYTEARRAY -> {
                byte[] bytes = new byte[(int) this.read7BitEncodedInt()];
                this.buf.readBytes(bytes);
                return (Tuple<T, String>) Tuple.of(bytes, objName);
            }
        }

        return null;
    }

}
