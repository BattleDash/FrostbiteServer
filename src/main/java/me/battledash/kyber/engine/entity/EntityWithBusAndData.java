package me.battledash.kyber.engine.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import me.battledash.kyber.types.pojo.GameObjectData;

@Getter
@EqualsAndHashCode(callSuper = true)
public abstract class EntityWithBusAndData<T extends GameObjectData> extends Entity {

    private EntityBus entityBus;
    private final T data;

    public EntityWithBusAndData(EntityCreationInfo info, T data) {
        super(info);
        this.data = data;
    }

    @Override
    public EntityOwner getOwner() {
        if (this.entityBus == null) {
            return null;
        }
        return this.entityBus.getOwner();
    }

}
