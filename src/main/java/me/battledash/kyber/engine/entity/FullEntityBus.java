package me.battledash.kyber.engine.entity;

import me.battledash.kyber.fs.ebx.EbxClassInstance;
import me.battledash.kyber.types.pojo.EntityBusData;

public class FullEntityBus extends EntityBus {

    public FullEntityBus(EntityOwner owner, EntityBus parent, Void parentRep,
                         EntityBusData busData, EbxClassInstance exposedData,
                         boolean isSubLevel, int laneId,
                         boolean createTransformSpaceObject) {
        super(owner, parent);
        this.setBusType(EntityBusType.FULL);
    }

}
