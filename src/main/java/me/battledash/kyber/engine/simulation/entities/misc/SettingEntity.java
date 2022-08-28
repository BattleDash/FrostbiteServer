package me.battledash.kyber.engine.simulation.entities.misc;

import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.entity.EntityCreationInfo;
import me.battledash.kyber.engine.entity.EntityEvent;
import me.battledash.kyber.engine.entity.EntityWithBusAndData;
import me.battledash.kyber.engine.entity.FrostbiteEntity;
import me.battledash.kyber.types.pojo.entities.SettingEntityData;

@Slf4j
@FrostbiteEntity(SettingEntityData.class)
public class SettingEntity extends EntityWithBusAndData<SettingEntityData> {

    public SettingEntity(EntityCreationInfo info, SettingEntityData data) {
        super(info, data);
    }

    @Override
    public void onCreate(EntityCreationInfo info) {
    }

    @Override
    public void event(EntityEvent event) {
    }

}