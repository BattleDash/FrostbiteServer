package me.battledash.kyber.engine.simulation.entities.synced;

import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.entity.EntityCreationInfo;
import me.battledash.kyber.engine.entity.EntityEvent;
import me.battledash.kyber.engine.entity.EntityInitInfo;
import me.battledash.kyber.engine.entity.EntityWithBusAndData;
import me.battledash.kyber.engine.entity.EventId;
import me.battledash.kyber.engine.entity.FrostbiteEntity;
import me.battledash.kyber.types.pojo.entities.CompareBoolEntityData;
import me.battledash.kyber.types.pojo.entities.PathfindingSystemEntityData;
import me.battledash.kyber.types.pojo.entities.SyncedBoolEntityData;

@Slf4j
@FrostbiteEntity(SyncedBoolEntityData.class)
public class SyncedBoolEntity extends EntityWithBusAndData<SyncedBoolEntityData> {

    public SyncedBoolEntity(EntityCreationInfo info, SyncedBoolEntityData data) {
        super(info, data);
    }

    @Override
    public void onCreate(EntityCreationInfo info) {
    }

    @Override
    public void event(EntityEvent event) {
    }

}