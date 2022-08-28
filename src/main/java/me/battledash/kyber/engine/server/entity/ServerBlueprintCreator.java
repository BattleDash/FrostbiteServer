package me.battledash.kyber.engine.server.entity;

import me.battledash.kyber.engine.entity.EntityBusPeer;
import me.battledash.kyber.engine.entity.EntityCreator;
import me.battledash.kyber.engine.entity.EntityFactory;
import me.battledash.kyber.types.pojo.entities.EntityData;

public class ServerBlueprintCreator extends EntityCreator {

    public ServerBlueprintCreator(Class<? extends EntityBusPeer> entityClass, Class<? extends EntityData> dataClass) {
        super(entityClass, dataClass, true);
    }

    @Override
    public void onInitLevel() {
        if (this.getEntityClass().getSimpleName().equals("ReferenceObjectData")) {
            super.onInitLevel();
        }
    }

    @Override
    public EntityCreator registerToData() {
        return EntityCreator.setCreator(this.getDataClass(), this);
    }

    @Override
    public EntityBusPeer create(EntityFactory.EntityFactoryParams data) {
        return null;
    }

    public static void registerCreators() {
    }

}
