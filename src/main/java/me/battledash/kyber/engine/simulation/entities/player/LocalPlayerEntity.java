package me.battledash.kyber.engine.simulation.entities.player;

import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.entity.EntityCreationInfo;
import me.battledash.kyber.engine.entity.EntityEvent;
import me.battledash.kyber.engine.entity.EntityWithBusAndData;
import me.battledash.kyber.engine.entity.FrostbiteEntity;
import me.battledash.kyber.types.pojo.entities.LocalPlayerEntityData;

@Slf4j
@FrostbiteEntity(LocalPlayerEntityData.class)
public class LocalPlayerEntity extends EntityWithBusAndData<LocalPlayerEntityData> {

    public LocalPlayerEntity(EntityCreationInfo info, LocalPlayerEntityData data) {
        super(info, data);
    }

    @Override
    public void onCreate(EntityCreationInfo info) {
    }

    @Override
    public void event(EntityEvent event) {
    }

}