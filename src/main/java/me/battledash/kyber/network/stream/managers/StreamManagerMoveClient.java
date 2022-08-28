package me.battledash.kyber.network.stream.managers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.network.stream.StreamManager;

@Slf4j
public class StreamManagerMoveClient extends StreamManager {

    private static final int MOVE_CONSTANTS_WINDOW_BIT_COUNT = 14;
    private static final int MOVE_CONSTANTS_WINDOW_SIZE = 1 << StreamManagerMoveClient.MOVE_CONSTANTS_WINDOW_BIT_COUNT;
    private static final int MOVE_CONSTANTS_WINDOW_MASK = StreamManagerMoveClient.MOVE_CONSTANTS_WINDOW_SIZE - 1;

    private static final int MOVE_CONSTANTS_DELTA_MOVE_WINDOW_BIT_COUNT = 6;
    private static final int MOVE_CONSTANTS_DELTA_MOVE_WINDOW_SIZE= 1 << StreamManagerMoveClient.MOVE_CONSTANTS_DELTA_MOVE_WINDOW_BIT_COUNT;
    private static final int MOVE_CONSTANTS_DELTA_MOVE_WINDOW_MASK = StreamManagerMoveClient.MOVE_CONSTANTS_DELTA_MOVE_WINDOW_SIZE - 1;
    private static final int MOVE_CONSTANTS_MAX_DEFAULT_MOVE_BIT_COUNT = 5;
    private static final int MOVE_CONSTANTS_MAX_DEFAULT_MOVE_COUNT = (1 << StreamManagerMoveClient.MOVE_CONSTANTS_MAX_DEFAULT_MOVE_BIT_COUNT) - 1;

    @Getter
    private final String name = "MoveClient";

    private long lastServerReceivedSequenceNumber;
    private long remoteFrequencyDivider;
    private long baseLineStateCount;

    @Override
    public boolean processReceivedPacket(BitStreamRead stream) {
        long sequenceNumberExecutedByServer = stream.readUnsigned(StreamManagerMoveClient.MOVE_CONSTANTS_WINDOW_BIT_COUNT);
        if (stream.readBool()) {
            this.lastServerReceivedSequenceNumber = stream.readUnsigned(StreamManagerMoveClient.MOVE_CONSTANTS_WINDOW_BIT_COUNT);
        }

        long forcedMoveCount = 0;
        if (stream.readUnsigned(1) > 0) {
            forcedMoveCount = stream.readUnsigned(StreamManagerMoveClient.MOVE_CONSTANTS_MAX_DEFAULT_MOVE_BIT_COUNT);
        } else {
            if (stream.readBool()) {
                long ticks = stream.readUnsigned(32);
            }
        }



        if (stream.readBool()) { // returnImmediately
            return true;
        }

        if (stream.readBool()) { // FrequencyDivider
            this.remoteFrequencyDivider = stream.readUnsigned(4);
        }

        if (stream.readBool()) { // compressionInfo?
            long baseLineStateCount = 0;
            if (stream.readBool()) {
                baseLineStateCount = stream.readUnsigned(8) + 1;
            }

            this.baseLineStateCount = baseLineStateCount;
            return true;
        }

        if (!stream.readBool()) { // use prediction?
            return true;
        }

        if (this.baseLineStateCount > 0) {
            this.readBaseline(stream);
        }

        return true;
    }

    private void readBaseline(BitStreamRead stream) {
        if (stream.readBool()) {
            log.info("Baseline");
        } else if (stream.readBool()) {
            log.info("Baseline2");
        }
    }

}
