package me.battledash.kyber.engine.simulation.entities.compare;

import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.entity.EntityCreationInfo;
import me.battledash.kyber.engine.entity.EntityEvent;
import me.battledash.kyber.engine.entity.EntityInitInfo;
import me.battledash.kyber.engine.entity.EntityWithBusAndData;
import me.battledash.kyber.engine.entity.EventId;
import me.battledash.kyber.engine.entity.FrostbiteEntity;
import me.battledash.kyber.types.pojo.entities.CompareBoolEntityData;

@Slf4j
@FrostbiteEntity(CompareBoolEntityData.class)
public class CompareBoolEntity extends EntityWithBusAndData<CompareBoolEntityData> {

    private Boolean lastResult;

    private boolean shouldFireOnInit;
    private boolean bool;

    public CompareBoolEntity(EntityCreationInfo info, CompareBoolEntityData data) {
        super(info, data);
    }

    @Override
    public void onCreate(EntityCreationInfo info) {
        this.shouldFireOnInit = this.getData().isTriggerOnStart();
        this.bool = this.getData().getBool();
    }

    @Override
    public void onInit(EntityInitInfo info) {
        super.onInit(info);

        if (this.shouldFireOnInit) {
            this.shouldFireOnInit = false;
            this.lastResult = this.bool;
            this.fireEvent(this.lastResult ? new EventId("OnTrue") : new EventId("OnFalse"));
        }
    }

    @Override
    public void event(EntityEvent event) {
    }

}