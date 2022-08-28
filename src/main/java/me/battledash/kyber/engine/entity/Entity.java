package me.battledash.kyber.engine.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
public abstract class Entity extends EntityBusPeer {

    @Getter
    private long flags;

    public Entity(EntityBusPeerCreationInfo info) {
        super(info);
    }

    public abstract EntityOwner getOwner();

    public void create(EntityCreationInfo info, boolean callOnCreate) {
        if (Flags.IS_CREATED.test(this.flags)) {
            throw new IllegalStateException("Entity is already created");
        }

        if (callOnCreate) {
            this.onCreate(info);
        }
    }

    @Override
    public void create(EntityCreationInfo info) {
        this.create(info, true);
    }

    public void onInit(EntityInitInfo info) {
    }

    public void init(EntityInitInfo info) {
        this.onInit(info);
    }

    public boolean isInitialized() {
        return Flags.IS_INITIALIZED.test(this.flags);
    }

    public boolean isCreated() {
        return Flags.IS_CREATED.test(this.flags);
    }

    public enum Flags {
        REALM,
        IS_CREATED,
        IS_REGISTERED_TO_BUS,
        IS_INITIALIZED,
        IS_ITERABLE,
        TRACK_NON_ITERABLE,
        IS_INIT_NOT_ALLOWED,
        IS_DEINIT_NOT_ALLOWED,
        IS_DESTROY_NOTALLOWED,
        IS_SAVED,
        IS_MARKED_FOR_RELEASE,
        IS_DELAYED_INIT_ALLOWED,
        HAS_BEEN_INITIALIZED,
        IS_ENTITY_OWNER,
        IS_IN_ENTITY_OWNER_RANGE,
        HAS_WEAKPTR_TOKEN;

        public int getFlag() {
            return 1 << this.ordinal();
        }

        public boolean test(long flags) {
            return (flags & this.getFlag()) != 0;
        }
    }

}
