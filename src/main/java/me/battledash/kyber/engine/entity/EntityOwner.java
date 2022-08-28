package me.battledash.kyber.engine.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.network.stream.managers.ghost.Ghost;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class EntityOwner extends ServerEntityOwnerBase {

    private final EntityOwner parent;
    private Ghost ghost;

    public Integer getUniqueId() {
        return this.internalGetUniqueId();
    }

    public abstract Integer internalGetUniqueId();

    public boolean isGhost() {
        return this.ghost != null;
    }

}
