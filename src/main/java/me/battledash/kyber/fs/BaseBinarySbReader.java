package me.battledash.kyber.fs;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.battledash.kyber.util.EndianUtil;
import me.battledash.kyber.util.Fnv1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BaseBinarySbReader implements BinarySbReader {

    private static final long MAGIC_EBX = 0xed1cedb8;
    private static final long MAGIC_DBX = 0xfe1fbeef;
    private static final long MAGIC_CAS = 0xed0eadde;
    private static final long MAGIC_KELVIN = 0xc3889333;

    private long totalCount;

    private long ebxCount;
    private long resCount;
    private long chunkCount;
    private long stringsOffset;
    private long metaOffset;
    private long metaSize;

    private final List<Sha1> sha1 = new ArrayList<>();

    private final EndianUtil.Endianness endian = EndianUtil.Endianness.BIG;

    @Override
    public DbObject readDbObject(DbReader reader, boolean containsUncompressedData, long bundleOffset) {
        ByteBuf buf = reader.getBuf();

        long dataOffset = reader.readUInt(EndianUtil.Endianness.BIG) + 4;
        long magic = reader.readUInt(this.endian) ^ 0x7065636E;

        boolean containsSha1 = !(magic == 0xC3889333 || magic == 0xC3E5D5C3);

        this.totalCount = buf.readUnsignedInt();
        this.ebxCount = buf.readUnsignedInt();
        this.resCount = buf.readUnsignedInt();
        this.chunkCount = buf.readUnsignedInt();
        this.stringsOffset = buf.readUnsignedInt() - 0x24;
        this.metaOffset = buf.readUnsignedInt() - 0x24;
        this.metaSize = buf.readUnsignedInt();

        byte[] buffer = new byte[(int)(dataOffset - buf.readerIndex())];
        buf.readBytes(buffer);

        if (magic == BaseBinarySbReader.MAGIC_KELVIN) {
            containsSha1 = false;
        } else if (magic == 0xC3E5D5C3) {
            containsSha1 = false;
            throw new IllegalStateException("Second magic is not supported yet");
        }

        DbObject bundle = new DbObject(new HashMap<>());
        {
            DbReader dbReader = new DbReader(Unpooled.wrappedBuffer(buffer), false);

            for (int i = 0; i < this.totalCount; i++) {
                this.sha1.add(containsSha1 ? dbReader.readSha1() : Sha1.ZERO);
            }

            bundle.addValue("ebx", new DbObject(this.readEbx(dbReader)));
            bundle.addValue("res", new DbObject(this.readRes(dbReader)));
            bundle.addValue("chunks", new DbObject(this.readChunks(dbReader)));
            bundle.addValue("dataOffset", (int) (dataOffset - 4));

            if (this.chunkCount > 0) {
                dbReader.getBuf().readerIndex((int) (this.metaOffset + 4));
                bundle.addValue("chunkMeta", dbReader.readDbObject());
            }
        }

        reader.getBuf().readerIndex((int) dataOffset);
        if (magic == 0xED1CEDB8 || reader.getBuf().readerIndex() == reader.getBuf().writerIndex()) {
            return bundle;
        }

        this.readDataBlock(reader, bundle.getValue("ebx"), containsUncompressedData, bundleOffset);
        this.readDataBlock(reader, bundle.getValue("res"), containsUncompressedData, bundleOffset);
        this.readDataBlock(reader, bundle.getValue("chunks"), containsUncompressedData, bundleOffset);

        Preconditions.checkState(reader.getBuf().readerIndex() <= reader.getBuf().writerIndex(),
                "Reader index is greater than writer index");
        return bundle;
    }

    @Override
    public void readDataBlock(DbReader reader, DbObject list, boolean containsUncompressedData, long bundleOffset) {
        for (DbObject entry : list.<DbObject>getList()) {
            entry.addValue("offset", bundleOffset + reader.getBuf().readerIndex());

            long originalSize = entry.getValue("originalSize");
            long size = 0;

            if (containsUncompressedData) {
                size = originalSize;
                entry.addValue("data", reader.getBuf().readBytes((int) originalSize));
            } else {
                while (originalSize > 0) {
                    int decompressedSize = reader.readInt(EndianUtil.Endianness.BIG);
                    int compressionType = reader.readUShort();
                    int bufferSize = reader.readUShort(EndianUtil.Endianness.BIG);

                    int flags = ((compressionType & 0xFF00) >> 8);

                    if ((flags & 0x0F) != 0) {
                        bufferSize = ((flags & 0x0F) << 0x10) + bufferSize;
                    }
                    if ((decompressedSize & 0xFF000000) != 0) {
                        decompressedSize &= 0x00FFFFFF;
                    }

                    originalSize -= decompressedSize;

                    compressionType = (compressionType & 0x7F);
                    if (compressionType == 0x00) {
                        bufferSize = decompressedSize;
                    }

                    size += bufferSize + 8;
                    reader.getBuf().readerIndex(bufferSize);
                }
            }

            entry.addValue("size", size);
            entry.addValue("sb", true);
        }
    }

    public List<Object> readEbx(DbReader reader) {
        List<Object> ebxList = new ArrayList<>();

        for (int i = 0; i < this.ebxCount; i++) {
            DbObject entry = new DbObject(new HashMap<>());

            long nameOffset = reader.readUInt(this.endian);
            long originalSize = reader.readUInt(this.endian);

            int currentPos = reader.getBuf().readerIndex();
            reader.getBuf().readerIndex((int) (4 + this.stringsOffset + nameOffset));

            entry.addValue("sha1", this.sha1.get(i));
            entry.addValue("name", reader.readNullTerminatedString());
            entry.addValue("nameHash", Fnv1.hashString(entry.getValue("name")));
            entry.addValue("originalSize", originalSize);
            ebxList.add(entry);

            reader.getBuf().readerIndex(currentPos);
        }

        return ebxList;
    }

    public List<Object> readRes(DbReader reader) {
        List<Object> resList = new ArrayList<>();
        int offset = (int) this.ebxCount;

        ByteBuf buf = reader.getBuf();
        for (int i = 0; i < this.resCount; i++) {
            DbObject entry = new DbObject(new HashMap<>());

            long nameOffset = buf.readUnsignedInt();
            long originalSize = buf.readUnsignedInt();

            int currentPos = buf.readerIndex();
            buf.readerIndex((int) (4 + this.stringsOffset + nameOffset));

            entry.addValue("sha1", this.sha1.get(offset++));
            entry.addValue("name", reader.readNullTerminatedString());
            entry.addValue("nameHash", Fnv1.hashString(entry.getValue("name")));
            entry.addValue("originalSize", originalSize);
            resList.add(entry);

            buf.readerIndex(currentPos);
        }

        for (Object res : resList) {
            ((DbObject) res).addValue("resType", buf.readUnsignedInt());
        }

        for (Object res : resList) {
            byte[] buffer = new byte[0x10];
            buf.readBytes(buffer);
            ((DbObject) res).addValue("resMeta", buffer);
        }

        for (Object res : resList) {
            ((DbObject) res).addValue("resType", buf.readLong());
        }

        return resList;
    }

    public List<Object> readChunks(DbReader reader) {
        List<Object> chunkList = new ArrayList<>();
        int offset = (int)(this.ebxCount + this.resCount);

        for (int i = 0; i < this.chunkCount; i++) {
            DbObject entry = new DbObject(new HashMap<>());

            UUID chunkId = reader.readGuid(EndianUtil.Endianness.BIG);
            long logicalOffset = reader.readUInt();
            long logicalSize = reader.readUInt();
            long originalSize = (logicalOffset & 0xFFFF) | logicalSize;

            entry.addValue("id", chunkId);
            entry.addValue("sha1", this.sha1.get(offset + i));
            entry.addValue("logicalOffset", logicalOffset);
            entry.addValue("logicalSize", logicalSize);
            entry.addValue("originalSize", originalSize);

            chunkList.add(entry);
        }

        return chunkList;
    }



}
