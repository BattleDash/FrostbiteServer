package me.battledash.kyber.network.stream.managers.ghost;

import lombok.Data;

@Data
public abstract class Ghost {

    private int ghostId;
    private boolean staticGhost;
    private boolean requiredForLevelSync;
    private int isDeltaCompressed;

    public int getNetClassId() {
        return GhostFactory.getGhostTypeId(this.getClass());
    }

    public abstract String getNetName();

    public abstract String getNetClassName();

}
