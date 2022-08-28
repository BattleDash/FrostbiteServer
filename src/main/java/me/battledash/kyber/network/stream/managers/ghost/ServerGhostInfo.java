package me.battledash.kyber.network.stream.managers.ghost;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.misc.StateMask;

@Data
@EqualsAndHashCode(callSuper = true)
public class ServerGhostInfo extends GhostInfo {

    private StateMask mask;
    private float frequencyFactor;
    private State state;
    private int sentInThisFrame = 1;
    private int currentBaselineIndex = 1;
    private int maySendNewBaseline = 1;
    private int skippedThisFrame = 1;
    private int filteredThisFrame = 1;
    private int filteredOwnedGhosts = 1;

    public enum State {
        UNKNOWN,
        CREATION_NOT_CONFIRMED,
        CREATION_CONFIRMED,
        DELETION_NOT_CONFIRMED
    }

}
