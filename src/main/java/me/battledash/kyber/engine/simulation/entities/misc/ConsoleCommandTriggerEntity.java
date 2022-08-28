package me.battledash.kyber.engine.simulation.entities.misc;

import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.entity.EntityCreationInfo;
import me.battledash.kyber.engine.entity.EntityEvent;
import me.battledash.kyber.engine.entity.EntityWithBusAndData;
import me.battledash.kyber.engine.entity.FrostbiteEntity;
import me.battledash.kyber.types.pojo.entities.ConsoleCommandTriggerEntityData;

@Slf4j
@FrostbiteEntity(ConsoleCommandTriggerEntityData.class)
public class ConsoleCommandTriggerEntity extends EntityWithBusAndData<ConsoleCommandTriggerEntityData> {

    private int argStreamCount;
    private int commandCount;

    public ConsoleCommandTriggerEntity(EntityCreationInfo info, ConsoleCommandTriggerEntityData data) {
        super(info, data);
    }

    @Override
    public void onCreate(EntityCreationInfo info) {
        log.info("Registering command {}", this.getData().getCommandName());
    }

    @Override
    public void event(EntityEvent event) {
    }

}