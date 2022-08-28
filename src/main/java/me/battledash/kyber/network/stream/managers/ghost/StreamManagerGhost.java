package me.battledash.kyber.network.stream.managers.ghost;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.misc.StateMask;
import me.battledash.kyber.network.state.SendStatus;
import me.battledash.kyber.network.stream.StreamManager;
import me.battledash.kyber.network.stream.TransmitResult;
import me.battledash.kyber.server.GameContext;
import me.battledash.kyber.server.ServerGameContext;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.streams.BitStreamRollback;
import me.battledash.kyber.streams.BitStreamWrite;

/**
 * Ghost handles replication of network objects like players and vehicles.
 */
@Slf4j
public class StreamManagerGhost extends StreamManager {

    private static final int GHOST_NET_SEND_TICK_BITS = 8;
    private static final int GHOST_SEQUENCE_NUMBER_BITS = 3;

    private static final int GHOST_BIT_COUNT = 11;

    @Getter
    private final String name = "Ghost";

    @Setter
    private GhostStatusInflicter statusInflicter;
    @Setter
    private GhostCreator creator;
    @Setter
    private GhostConnection ghostConnection;

    private float frameTime;

    @Setter
    private float serverFrameTime;

    private int sequenceNum = 0;
    private int receivedSequenceNum;

    private int supportsDeltaCompression = 0;
    private int remoteSupportsDeltaCompression;
    private int ghostBlockCount = 0;

    {
        this.setDebugBroadcastProcessing(false);
    }

    private boolean tempSent; // TODO: 5/12/2022 Remove this

    @SneakyThrows
    @Override
    public TransmitResult transmitPacket(BitStreamWrite stream) {
        if (this.statusInflicter == null) {
            return TransmitResult.NOTHING_TO_SEND;
        }

        SendStatus sendStatus = this.statusInflicter.getSendStatus();
        if (sendStatus == SendStatus.DO_NOT_SEND) {
            return TransmitResult.NOTHING_TO_SEND;
        }

        this.sequenceNum = (this.sequenceNum + 1) & ((1 << StreamManagerGhost.GHOST_SEQUENCE_NUMBER_BITS) - 1);

        {
            if (sendStatus == SendStatus.SEND_ALL) {

            }
        }

        this.ghostBlockCount = 0;
        BitStreamRollback headerRollback = new BitStreamRollback(stream);
        stream.writeUnsignedFloat(this.serverFrameTime);

        stream.writeBits(this.sequenceNum, StreamManagerGhost.GHOST_SEQUENCE_NUMBER_BITS);

        {
            stream.writeBits(0, StreamManagerGhost.GHOST_NET_SEND_TICK_BITS); // TODO: 5/8/2022 - Ghost net send tick
        }

        stream.writeUnsigned(this.supportsDeltaCompression, 1);

        if (this.supportsDeltaCompression > 0) {
            stream.writeUnsigned(1, 32);
        }

        int origPos = stream.tell();

        stream.writeUnsigned(this.tempSent ? 0 : 1, StreamManagerGhost.GHOST_BIT_COUNT);
        if (stream.isOverflown()) {
            return TransmitResult.NOTHING_TO_SEND;
        }

        if (!this.tempSent) {
            this.tempSent = true;
            stream.writeBool(false);
            ServerGhostInfo info = new ServerGhostInfo();
            info.setMask(new StateMask());
            info.getMask().setBits(StateMask.GHOST_MASK_INIT, StateMask.RESERVED_STATES, 0);
            info.setState(ServerGhostInfo.State.CREATION_NOT_CONFIRMED);
            info.setGhost(ServerGameContext.context().getServerLevelSubLevel().getGhost());
            this.sendNewOrExisting(stream, info);
        }

        if ((this.serverFrameTime == 0.f) & (this.ghostBlockCount <= 0)) {
            headerRollback.rollback();
            return TransmitResult.NOTHING_TO_SEND;
        }

        return TransmitResult.WHOLE;
    }

    @Override
    public boolean processReceivedPacket(BitStreamRead stream) {
        if (!GameContext.SIMULATE_CLIENT) {
            return true;
        }

        this.frameTime = stream.readUnsignedFloat();

        log.debug("Received frame time {}", this.frameTime);

        {
            int prevSequenceNum = this.receivedSequenceNum;
            this.receivedSequenceNum = (int) stream.readBits(StreamManagerGhost.GHOST_SEQUENCE_NUMBER_BITS);
            long sequenceDiff = ((this.receivedSequenceNum - prevSequenceNum) & ((1 << StreamManagerGhost.GHOST_SEQUENCE_NUMBER_BITS) - 1));
            long netTickDff = stream.readBits(StreamManagerGhost.GHOST_NET_SEND_TICK_BITS);
            // TODO: 5/8/2022 newStateNotification
        }

        //this.remoteSupportsDeltaCompression = (int) stream.readUnsigned(1);
        this.remoteSupportsDeltaCompression = 0;

        if (stream.readBool()) {
            stream.readUnsigned(32);
        }

        long ghostCount = stream.readUnsigned(StreamManagerGhost.GHOST_BIT_COUNT);

        if (ghostCount > 0) {
            log.info("Received {} ghosts ({})", ghostCount, this.remoteSupportsDeltaCompression);
        }

        int i;
        for (i = 0; i < ghostCount; i++) {
            if (!stream.readBool()) {
                break;
            }

            if (!this.receiveDeleted(stream)) {
                return false;
            }
        }

        if (i < ghostCount) {
            for (; i < ghostCount; i++) {
                if (!this.receiveNewOrExisting(stream)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean sendNewOrExisting(BitStreamWrite stream, ServerGhostInfo info) {
        ServerGhost ghost = (ServerGhost) info.getGhost();
        if (!info.getMask().testBits(StateMask.GHOST_MASK_INIT, StateMask.RESERVED_STATES, 0) &
                (info.getState() == ServerGhostInfo.State.CREATION_NOT_CONFIRMED)) {
            log.warn("Skipping ghost {} because it is not in the correct state", ghost);
            return true;
        }

        boolean hasSkippedDependency = false;
        boolean hasFilteredDependency = false;

        if (!info.getMask().isZero()) {
            boolean isInitialState = info.getState() != ServerGhostInfo.State.CREATION_CONFIRMED;

            int ghostId = ghost.getGhostId();
            ghostId = 50;

            stream.writeUnsigned(ghostId, StreamManagerGhost.GHOST_BIT_COUNT);

            if (stream.writeBool(isInitialState)) {
                this.creator.writeGhostHeader(stream, ghost);
                // TODO: 5/10/2022 Net init data
                ghost.writeNetworkables(stream, this.ghostConnection);
            }

            stream.writeUnsigned(0, 1);

            this.ghostBlockCount++;
        }

        return true;
    }

    private boolean receiveNewOrExisting(BitStreamRead stream) {
        int ghostId = (int) stream.readUnsigned(StreamManagerGhost.GHOST_BIT_COUNT);

        Ghost ghost = null;

        boolean isInitialState = stream.readBool();
        if (isInitialState) {
            GhostCreator.GhostCreatorResult result = new GhostCreator.GhostCreatorResult();
            this.creator.createGhost(stream, ghostId, result);
            ghost = result.getGhost();
            ghost.setGhostId(ghostId);
        } else {
            log.info("Receiving existing ghost with id {}", ghostId);
        }

        {
            if ((this.remoteSupportsDeltaCompression & ghost.getIsDeltaCompressed()) > 0) {
                int baselineIndex = (int) stream.readUnsigned(1);
                int newBaselineWillBeStored = (int) stream.readUnsigned(1);
            }

            long l = stream.readUnsigned(8);
            log.info("Received {} bytes of network data", l);

            //stream.readUnsigned(3);
        }

        return true;
    }

    private boolean receiveDeleted(BitStreamRead stream) {
        int ghostId = (int) stream.readUnsigned(StreamManagerGhost.GHOST_BIT_COUNT);
        log.info("Received deleted ghost {}", ghostId);
        return false;
    }

    public ClientGhost readGhostId(BitStreamRead stream) {
        int ghostId = (int) stream.readUnsigned(StreamManagerGhost.GHOST_BIT_COUNT);
        if (ghostId > 0) {
            // TODO: 5/9/2022 - Read ghost id
        }
        return null;
    }

    public void writeGhostId(BitStreamWrite stream, ServerGhost ghost) {
        int id = 0;
        if (ghost != null) {

        }

        stream.writeUnsigned(ghost.getGhostId(), StreamManagerGhost.GHOST_BIT_COUNT);
    }

}
