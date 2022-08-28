package me.battledash.kyber.fs;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.Getter;

import java.util.Objects;

@Getter
public class CatReader extends NativeReader {

    private static final String MAGIC = "NyanNyanNyanNyan";

    private long resourceCount;
    private long patchCount;
    private long encryptedCount;

    public CatReader(ByteBuf buf) {
        super(buf);

        int position = 0;
        String magic;
        while (true) {
            magic = this.readSizedString(16);
            if (CatReader.MAGIC.equals(magic)) {
                break;
            }
            this.buf.readerIndex(++position);
        }

        this.resourceCount = this.readUInt();
        this.patchCount = this.readUInt();
        this.encryptedCount = this.readUInt();
        this.getBuf().skipBytes(12);
        this.encryptedCount = 0;
    }

    public CatResourceEntry readResourceEntry() {
        CatResourceEntry entry = new CatResourceEntry();
        entry.setSha1(this.readSha1());
        entry.setOffset(this.readUInt());
        entry.setSize(this.readUInt());
        entry.setLogicalOffset(this.readUInt());
        entry.setArchiveIndex(this.readInt() & 0xFF);
        return entry;
    }

    public CatPatchEntry readPatchEntry() {
        CatPatchEntry entry = new CatPatchEntry();
        entry.setSha1(this.readSha1());
        entry.setBaseSha1(this.readSha1());
        entry.setDeltaSha1(this.readSha1());
        return entry;
    }

    @Data
    public static class CatPatchEntry {
        private Sha1 sha1;
        private Sha1 baseSha1;
        private Sha1 deltaSha1;
    }

    @Data
    public static class CatResourceEntry {
        private Sha1 sha1;
        private long offset;
        private long size;
        private long logicalOffset;
        private int archiveIndex;

        private boolean isEncrypted;
        private long unknown;
        private String keyId;
        private byte[] unknownData;
        private long encryptedSize;
    }

}
