package me.battledash.kyber.network.stream.managers;

import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.misc.Buffer;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.network.stream.StreamManager;

@Slf4j
public class StreamManagerFile extends StreamManager {

    private static final DownloadState[] DOWNLOAD_STATES = DownloadState.values();

    private static final int MAX_FILE_SIZE = 8 << 20;
    private static final int SEGMENT_SIZE = 128;
    private static final int MAX_SEGMENT_COUNT = (StreamManagerFile.MAX_FILE_SIZE + StreamManagerFile.SEGMENT_SIZE) / StreamManagerFile.SEGMENT_SIZE;
    private static final int MAX_SEGMENT_SEND_COUNT = 16;

    @Getter
    private final String name = "File";

    private final Buffer receiveBuffer = new Buffer();
    private DownloadState receiveState = DownloadState.READY;

    private int receiveSegmentCount;
    private int receiveSegmentsLeft;

    @Override
    public boolean processReceivedPacket(BitStreamRead stream) {
        boolean success;

        DownloadState state = StreamManagerFile.DOWNLOAD_STATES[(int) stream.readUnsignedLimit(0, DownloadState.COUNT.ordinal() - 1)];

        log.debug("Received packet with state {}", state);
        switch (state) {
            case SEND_FILE_INFO -> {
                success = this.processFileInfo(stream);
                this.receiveState = DownloadState.TRANSFER;
            }
            case TRANSFER -> {
                success = this.processTransfer(stream);
                this.receiveState = DownloadState.TRANSFER;
            }
            case CLOSE_TRANSFER -> {
                success = this.processClose(stream);
                this.receiveState = DownloadState.READY;
            }
            default -> success = false;
        }

        return success;
    }

    private boolean processClose(BitStreamRead stream) {
        if (this.receiveBuffer.getData() != null) {
            boolean deleteData = true;

            log.info("Downloaded file {} ({}kb)", this.receiveBuffer.getName(), this.receiveBuffer.getSize() / 1024);

            if (deleteData) {
                this.receiveBuffer.getData().release();
            }

            this.receiveBuffer.setData(null);
            this.receiveBuffer.setSize(0);
        }

        return true;
    }

    private boolean processTransfer(BitStreamRead stream) {
        int pos = (int) stream.readUnsignedLimit(0, this.receiveSegmentCount - 1);
        int segmentCount = (int) (1 + stream.readUnsignedLimit(0, StreamManagerFile.MAX_SEGMENT_SEND_COUNT - 1));

        for (int i = 0; i < segmentCount; i++) {
            int bufferPos = pos * StreamManagerFile.SEGMENT_SIZE;
            if (bufferPos >= this.receiveBuffer.getSize()) {
                log.warn("Received segment out of bounds");
                return false;
            }

            int count = Math.abs(Math.min(StreamManagerFile.SEGMENT_SIZE, this.receiveBuffer.getSize() - bufferPos));
            this.receiveBuffer.getData().setBytes(bufferPos, stream.getStream().readOctets(count));
            this.receiveSegmentsLeft--;
            pos++;

            log.debug("Received segment {}/{} with {}kb", i, segmentCount, count);
        }

        return true;
    }

    private boolean processFileInfo(BitStreamRead stream) {
        if (this.receiveBuffer.getData() != null) {
            log.warn("Received file info while already downloading a file");
            return false;
        }

        this.receiveBuffer.setName(stream.readString());
        this.receiveBuffer.setSize((int) stream.readUnsignedLimit(0, StreamManagerFile.MAX_FILE_SIZE));
        if (this.receiveBuffer.getSize() > 0) {
            this.receiveBuffer.setData(Unpooled.buffer(this.receiveBuffer.getSize()));
        }

        this.receiveSegmentCount = (this.receiveBuffer.getSize() + StreamManagerFile.SEGMENT_SIZE - 1) / StreamManagerFile.SEGMENT_SIZE;
        this.receiveSegmentsLeft = this.receiveSegmentCount;

        log.info("Downloading new file {} ({}kb)", this.receiveBuffer.getName(), this.receiveBuffer.getSize() / 1024);

        return true;
    }

    private enum DownloadState {
        READY,
        SEND_FILE_INFO,
        TRANSFER,
        CLOSE_TRANSFER,
        COUNT,
        WAIT_FOR_SEND_FILE_INFO_ACK,
        WAIT_FOR_CLOSE_TRANSFER_ACK
    }

}
