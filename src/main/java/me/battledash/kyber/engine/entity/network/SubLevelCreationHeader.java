package me.battledash.kyber.engine.entity.network;

import lombok.Data;
import me.battledash.kyber.engine.entity.EntityBus;

@Data
public class SubLevelCreationHeader {

    private EntityBus bus;
    private int subLevelId;
    private long uid;
    private long networkedBusCount;
    private long networkRegistryHash;
    private long firstDestructionCallbackId;

}
