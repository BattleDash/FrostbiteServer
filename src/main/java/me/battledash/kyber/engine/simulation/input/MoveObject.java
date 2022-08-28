package me.battledash.kyber.engine.simulation.input;

import lombok.Data;
import me.battledash.kyber.network.stream.managers.ghost.GhostConnection;
import me.battledash.kyber.streams.BitStreamRead;

@Data
public abstract class MoveObject {

    private long sequenceNumber;

    public abstract boolean moveRead(GhostConnection ghostConnection, BitStreamRead stream);

    public abstract boolean moveReadDelta(GhostConnection ghostConnection, BitStreamRead stream, MoveObject baseObject, MoveObject prevObject, boolean isFirstMove);

}
