package me.battledash.kyber.engine.simulation.entities.misc;

import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.entity.EntityCreationInfo;
import me.battledash.kyber.engine.entity.EntityEvent;
import me.battledash.kyber.engine.entity.EntityInitInfo;
import me.battledash.kyber.engine.entity.EntityWithBusAndData;
import me.battledash.kyber.engine.entity.FrostbiteEntity;
import me.battledash.kyber.types.pojo.entities.CompareBoolEntityData;
import me.battledash.kyber.types.pojo.entities.DelayEntityData;
import me.battledash.kyber.types.pojo.entities.PathfindingSystemEntityData;

@Slf4j
@FrostbiteEntity(DelayEntityData.class)
public class DelayEntity extends EntityWithBusAndData<DelayEntityData> {

    private int exchangeEventStartId;
    private int addedToUpdaterCount;
    private float delay;
    private boolean hasEventTriggered;

    public DelayEntity(EntityCreationInfo info, DelayEntityData data) {
        super(info, data);
    }

    @Override
    public void onCreate(EntityCreationInfo info) {
        this.delay = this.getData().getDelay();

        if (this.getData().isAutoStart()) {
            EntityEvent entityEvent;

        }
    }

    @Override
    public void onInit(EntityInitInfo info) {
    }

    @Override
    public void event(EntityEvent event) {
    }

}