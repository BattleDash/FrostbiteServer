package me.battledash.kyber.network.stream.managers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.simulation.input.MoveObject;
import me.battledash.kyber.network.PacketConnection;
import me.battledash.kyber.network.stream.StreamManager;
import me.battledash.kyber.streams.BitStreamRead;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class StreamManagerMoveServer extends StreamManager {

    private static final int MOVE_CONSTANTS_WINDOW_BIT_COUNT = 14;
    private static final int MOVE_CONSTANTS_WINDOW_SIZE = 1 << StreamManagerMoveServer.MOVE_CONSTANTS_WINDOW_BIT_COUNT;
    private static final int MOVE_CONSTANTS_WINDOW_MASK = StreamManagerMoveServer.MOVE_CONSTANTS_WINDOW_SIZE - 1;

    private static final int MOVE_CONSTANTS_DELTA_MOVE_WINDOW_BIT_COUNT = 7;
    private static final int MOVE_CONSTANTS_DELTA_MOVE_WINDOW_SIZE = 1 << StreamManagerMoveServer.MOVE_CONSTANTS_DELTA_MOVE_WINDOW_BIT_COUNT;
    private static final int MOVE_CONSTANTS_DELTA_MOVE_WINDOW_MASK = StreamManagerMoveServer.MOVE_CONSTANTS_DELTA_MOVE_WINDOW_SIZE - 1;
    private static final int MOVE_CONSTANTS_MAX_DEFAULT_MOVE_BIT_COUNT = 5;
    private static final int MOVE_CONSTANTS_MAX_DEFAULT_MOVE_COUNT = (1 << StreamManagerMoveServer.MOVE_CONSTANTS_MAX_DEFAULT_MOVE_BIT_COUNT) - 1;

    private static final int FIRST_IN_FRAME = 0x00000001;
    private static final int LAST_IN_FRAME = StreamManagerMoveServer.FIRST_IN_FRAME << 1;

    @Getter
    private final String name = "MoveServer";

    private final PacketConnection controlObject;
    private final MoveObject moveObject;
    private final Map<Integer, MoveObject> baseMoves = new HashMap<>();
    private final long baseMoveWindowSize = StreamManagerMoveServer.MOVE_CONSTANTS_DELTA_MOVE_WINDOW_SIZE / 2;
    private final long baseMoveWindowMask = this.baseMoveWindowSize - 1;

    private MoveObject baseMove;
    private long lastReceivedSequenceNumber = 0;

    private boolean setBaseMove(long sequence) {
        MoveObject newBase = this.baseMoves.get((int) (sequence & this.baseMoveWindowMask));
        this.baseMove = (newBase != null && newBase.getSequenceNumber() == sequence) ? newBase : null;

        if (this.baseMove == null) {
            log.warn("Base move not found for sequence #{}", sequence);
        }

        return this.baseMove != null;
    }

    @Override
    public boolean processReceivedPacket(BitStreamRead stream) {
        long pos = stream.getStream().getStreamBitReadPosition();
        if (stream.readBool()) {
            long baseLineStateCount = 0;
            if (stream.readBool()) {
                baseLineStateCount = stream.readUnsigned(8) + 1;
            }
            // TODO: 4/6/2022 Compression info
            return true;
        }

        long sequenceNumber = 0;
        MoveObject prevMove = null;
        {
            sequenceNumber = stream.readUnsigned(StreamManagerMoveServer.MOVE_CONSTANTS_WINDOW_BIT_COUNT);

            if (stream.readBool()) {
                long baseSequence = sequenceNumber - stream.readUnsigned(StreamManagerMoveServer.MOVE_CONSTANTS_DELTA_MOVE_WINDOW_BIT_COUNT);
                baseSequence &= StreamManagerMoveServer.MOVE_CONSTANTS_WINDOW_MASK;
                // TODO: 4/6/2022 Set base move

                if (!this.setBaseMove(baseSequence)) {
                    return false;
                }

                prevMove = this.baseMove;
            }
        }

        long movesExecuted = 0;
        boolean isFirstMove = true;
        boolean moveFollows = stream.readBool();
        MoveObject baseMove = prevMove;
        while (moveFollows) {
            if (prevMove != null) {
                if (!this.moveObject.moveReadDelta(null, stream, baseMove, prevMove, isFirstMove)) {
                    return false;
                }
            } else {
                if (!this.moveObject.moveRead(null, stream)) {
                    return false;
                }
            }
            isFirstMove = false;
            moveFollows = stream.readBool();

            this.moveObject.setSequenceNumber(sequenceNumber);
            prevMove = this.moveObject;

            long sequenceDiff = Math.min(sequenceNumber - this.lastReceivedSequenceNumber,
                    sequenceNumber + StreamManagerMoveServer.MOVE_CONSTANTS_DELTA_MOVE_WINDOW_SIZE - this.lastReceivedSequenceNumber);
            if ((sequenceDiff > 0) & (sequenceDiff <= StreamManagerMoveServer.MOVE_CONSTANTS_DELTA_MOVE_WINDOW_SIZE / 2)) {
                this.lastReceivedSequenceNumber = sequenceNumber;

                if (sequenceDiff > 1) {
                    log.info("Dropped {} moves", sequenceDiff - 1);
                }
                if (this.controlObject != null) {
                    movesExecuted++;

                    long flags = (movesExecuted == 1 ? 1 : 0) * StreamManagerMoveServer.FIRST_IN_FRAME;
                    flags |= (movesExecuted == 0 ? 1 : 0) * StreamManagerMoveServer.LAST_IN_FRAME;

                    this.baseMoves.put((int) (sequenceNumber & this.baseMoveWindowMask), this.controlObject.cloneMove(this.moveObject));
                    log.info("Added move #{}", sequenceNumber);
                }
            }

            sequenceNumber = (sequenceNumber + 1) & StreamManagerMoveServer.MOVE_CONSTANTS_WINDOW_MASK;
        }

        if (movesExecuted > 8) {
            log.warn("Executed {} moves on a single frame!", movesExecuted);
        }

        log.info("Read {} bits over {} moves", stream.getStream().getStreamBitReadPosition() - pos, movesExecuted);
        return true;
    }

}
