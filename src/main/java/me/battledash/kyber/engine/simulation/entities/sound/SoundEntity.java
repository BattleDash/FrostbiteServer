package me.battledash.kyber.engine.simulation.entities.sound;

import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.entity.EntityCreationInfo;
import me.battledash.kyber.engine.entity.EntityEvent;
import me.battledash.kyber.engine.entity.EntityWithBusAndData;
import me.battledash.kyber.engine.entity.FrostbiteEntity;
import me.battledash.kyber.types.pojo.entities.SoundEntityData;

@Slf4j
@FrostbiteEntity(SoundEntityData.class)
public class SoundEntity extends EntityWithBusAndData<SoundEntityData> {

    public SoundEntity(EntityCreationInfo info, SoundEntityData data) {
        super(info, data);
    }

    @Override
    public void onCreate(EntityCreationInfo info) {
    }

    @Override
    public void event(EntityEvent event) {
    }

}