package me.battledash.kyber.engine.entity;

import com.google.common.base.Preconditions;
import lombok.Data;
import me.battledash.kyber.types.pojo.GameObjectData;

public abstract class EntityBusPeer {

    public EntityBusPeer(EntityBusPeerCreationInfo info) {
    }

    public void create(EntityCreationInfo info) {
        this.onCreate(info);
    }

    public void destroy() {
        this.onDestroy();
    }

    public abstract void onCreate(EntityCreationInfo info);

    public abstract void event(EntityEvent event);

    public void fireEvent(EventId eventId) {
        this.internalFireEvent(this.getEntityBus(), this.getData(), eventId);
    }

    public void fireEvent(EntityEvent eventData) {
        this.internalFireEvent(this.getEntityBus(), this.getData(), eventData);
    }

    public void fireEvent(EventId newEventId, EntityEvent eventData) {
        this.internalFireEvent(this.getEntityBus(), this.getData(), newEventId, eventData);
    }

    public void onDestroy() {
    }

    public abstract EntityBus getEntityBus();

    public abstract GameObjectData getData();

    private void internalFireEvent(EntityBus bus, GameObjectData data, EventId eventId) {
        Preconditions.checkNotNull(bus);

        // TODO: 4/15/2022 Fire bus event
        throw new UnsupportedOperationException();
    }

    private void internalFireEvent(EntityBus bus, GameObjectData data, EntityEvent eventId) {
        Preconditions.checkNotNull(bus);

        // TODO: 4/15/2022 Fire bus event
        throw new UnsupportedOperationException();
    }

    private void internalFireEvent(EntityBus bus, GameObjectData data, EventId newEventId, EntityEvent eventData) {
        Preconditions.checkNotNull(bus);

        EventId originalEventId = eventData.getEventId();
        eventData.setEventId(newEventId);
        this.internalFireEvent(bus, data, eventData);
        eventData.setEventId(originalEventId);
    }

    @Data
    public static class EntityBusPeerCreationInfo {
        private final EntityBus bus;
        private final EntityCreatorType creatorType;
    }

    public enum EntityCreatorType {
        UNKNOWN,
        LEVEL,
        SPAWNER,
        OWNER,
        GHOST,
    }

}
