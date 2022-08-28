package me.battledash.kyber.engine.simulation.entities.sound;

import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.entity.EntityCreationInfo;
import me.battledash.kyber.engine.entity.EntityEvent;
import me.battledash.kyber.engine.entity.EntityWithBusAndData;
import me.battledash.kyber.engine.entity.FrostbiteEntity;
import me.battledash.kyber.types.pojo.entities.MixerEntityData;
import me.battledash.kyber.types.pojo.entities.SoundEntityData;

@Slf4j
@FrostbiteEntity(MixerEntityData.class)
public class MixerEntity extends EntityWithBusAndData<MixerEntityData> {

    public MixerEntity(EntityCreationInfo info, MixerEntityData data) {
        super(info, data);
    }

    @Override
    public void onCreate(EntityCreationInfo info) {
    }

    @Override
    public void event(EntityEvent event) {
    }

}