package me.battledash.kyber.engine.entity;

import lombok.Getter;
import lombok.Setter;
import me.battledash.kyber.types.pojo.LinearTransform;

@Getter
@Setter
public class EntityCreationInfo extends EntityBusPeer.EntityBusPeerCreationInfo {

    private int localPlayerId;
    private LinearTransform transform;
    private EntityBusPeer parent;
    private boolean isEntityOwner;
    private short collectionCount;
    private short transformCount;
    private boolean isAutoLoaded;

    public EntityCreationInfo(EntityBus bus, EntityBusPeer.EntityCreatorType creatorType) {
        super(bus, creatorType);
    }

}
