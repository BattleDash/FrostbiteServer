package me.battledash.kyber.engine.entity;

import lombok.Data;

@Data
public class ServerEntityOwnerBase {

    private int networkedBusCount;

    public void registerEntityBus(EntityBus bus) {
        bus.setNetworkId(++this.networkedBusCount);
    }

}
