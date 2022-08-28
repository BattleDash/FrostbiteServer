package me.battledash.kyber.engine.entity;

import lombok.Data;
import me.battledash.kyber.types.pojo.EntityBusData;
import me.battledash.kyber.types.pojo.LinearTransform;
import me.battledash.kyber.types.pojo.level.SubWorldData;

@Data
public class EntityBus {

    public static EntityBus createRootBus(EntityOwner owner, EntityBusData busData) {
        return new FullEntityBus(owner, null, null, busData, null,
                busData instanceof SubWorldData, 0, false);
    }

    private final EntityOwner owner;
    private final EntityBus parentBus;
    private EntityBusType busType;
    private int networkId;

    public boolean isProxyBus() {
        return this.busType == EntityBusType.PROXY;
    }

    public static boolean isWorldSpaceTransform(LinearTransform transform) {
        return true;
    }

}
