package me.battledash.kyber.fs;

import com.github.luben.zstd.Zstd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.util.EndianUtil;

import java.nio.ByteBuffer;

@Slf4j
public class CasReader extends NativeReader {

    private final ByteBuf deltaStream;

    public CasReader(ByteBuf buf, ByteBuf deltaStream) {
        super(buf);
        this.deltaStream = deltaStream == null ? null : deltaStream.retainedDuplicate();
    }

    public CasReader(ByteBuf buf) {
        this(buf, null);
    }

    public ByteBuf read() {
        ByteBuf outStream = Unpooled.buffer();

        if (this.deltaStream != null) {
            ByteBuf patchBuf = this.readPatched();
            outStream.writeBytes(patchBuf);
        }

        if (this.getBuf() != null) {
            while (this.getBuf().readerIndex() < this.getBuf().writerIndex()) {
                ByteBuf subBuffer = this.readBlock();
                if (subBuffer == null) {
                    break;
                }
                outStream.writeBytes(subBuffer);
                subBuffer.release();
            }
        }

        return outStream;
    }

    private ByteBuf readBlock() {
        int decompressedSize = this.readInt(EndianUtil.Endianness.BIG);
        int compressionType = this.readUShort();
        int bufferSize = this.readUShort(EndianUtil.Endianness.BIG);

        int flags = ((compressionType & 0xFF00) >> 8);
        ByteBuf buffer = null;

        if ((flags & 0x0F) != 0) {
            bufferSize = ((flags & 0x0F) << 0x10) + bufferSize;
        }
        if ((decompressedSize & 0xFF000000) != 0) {
            decompressedSize &= 0x00FFFFFF;
        }

        compressionType = (compressionType & 0x7F);

        if (compressionType == 0x0f) {
            buffer = this.decompressBlockZStd(bufferSize, decompressedSize);
        } else if (compressionType == 0x00) { // Uncompressed
            buffer = this.getBuf().readBytes(bufferSize);
        } else {
            log.warn("Got unknown block type: {}", compressionType);
        }

        return buffer;
    }

    private ByteBuf decompressBlockZStd(int bufferSize, int decompressedSize) {
        ByteBuf encoded = this.getBuf().readBytes(bufferSize);
        ByteBuf uncompressed = PooledByteBufAllocator.DEFAULT.directBuffer(decompressedSize, decompressedSize);

        if (encoded.hasMemoryAddress()) {
            Zstd.decompressUnsafe(uncompressed.memoryAddress(), decompressedSize,
                    encoded.memoryAddress() + encoded.readerIndex(),
                    encoded.readableBytes());
        } else {
            ByteBuffer uncompressedNio = uncompressed.nioBuffer(0, decompressedSize);
            ByteBuffer encodedNio = encoded.nioBuffer(encoded.readerIndex(), encoded.readableBytes());

            Zstd.decompress(uncompressedNio, encodedNio);
        }

        uncompressed.writerIndex(decompressedSize);
        encoded.release();
        return uncompressed;
    }

    private ByteBuf readPatched() {
        ByteBuf outStream = Unpooled.buffer();
        CasReader reader = new CasReader(this.deltaStream);
        while (reader.getBuf().readerIndex() < reader.getBuf().writerIndex()) {
            long tmpVal = reader.readUInt(EndianUtil.Endianness.BIG);
            int blockType = (int) (tmpVal & 0xF0000000) >> 28;

            if (blockType == 0x00) { // Read base blocks
                int blockCount = (int)(tmpVal & 0x0FFFFFFF);
                while (blockCount-- > 0) {
                    ByteBuf tmpBuffer = this.readBlock();
                    outStream.writeBytes(tmpBuffer);
                    tmpBuffer.release();
                }
            }
        }
        return null;
    }

}
