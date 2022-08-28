package me.battledash.kyber.engine.simulation.entities.debug;

import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.entity.EntityCreationInfo;
import me.battledash.kyber.engine.entity.EntityEvent;
import me.battledash.kyber.engine.entity.EntityWithBusAndData;
import me.battledash.kyber.engine.entity.FrostbiteEntity;
import me.battledash.kyber.types.pojo.entities.PrintDebugTextEntityData;

@Slf4j
@FrostbiteEntity(PrintDebugTextEntityData.class)
public class PrintDebugTextEntity extends EntityWithBusAndData<PrintDebugTextEntityData> {

    public PrintDebugTextEntity(EntityCreationInfo info, PrintDebugTextEntityData data) {
        super(info, data);
    }

    @Override
    public void onCreate(EntityCreationInfo info) {
    }

    @Override
    public void event(EntityEvent event) {
    }

}