package me.battledash.kyber.engine.simulation.entities.world;

import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.entity.EntityCreationInfo;
import me.battledash.kyber.engine.entity.EntityEvent;
import me.battledash.kyber.engine.entity.EntityWithBusAndData;
import me.battledash.kyber.engine.entity.FrostbiteEntity;
import me.battledash.kyber.types.pojo.entities.SchematicChannelEntityData;

@Slf4j
@FrostbiteEntity(SchematicChannelEntityData.class)
public class SchematicChannelEntity extends EntityWithBusAndData<SchematicChannelEntityData> {

    public SchematicChannelEntity(EntityCreationInfo info, SchematicChannelEntityData data) {
        super(info, data);
    }

    @Override
    public void onCreate(EntityCreationInfo info) {
    }

    @Override
    public void event(EntityEvent event) {
    }

}