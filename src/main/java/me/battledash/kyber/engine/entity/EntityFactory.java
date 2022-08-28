package me.battledash.kyber.engine.entity;

import com.google.common.base.Preconditions;
import lombok.Getter;
import me.battledash.kyber.types.pojo.GameObjectData;
import me.battledash.kyber.types.pojo.LinearTransform;
import me.battledash.kyber.types.pojo.entities.SpatialEntityData;

public class EntityFactory {

    public static EntityBusPeer internalCreateEntity(EntityFactoryParams params) {
        GameObjectData data = params.getData();

        EntityCreator creator = EntityCreator.getCreator(data.getClass());

        Preconditions.checkNotNull(creator, "No creator found for type '" + data.getType() + "'");

        switch (creator.getSpecialType()) {
            case SPATIAL_ENTITY -> {
                LinearTransform oldTransform = params.getTransform();
                LinearTransform localTransform = ((SpatialEntityData) data).getTransform();
                if (EntityBus.isWorldSpaceTransform(localTransform)) {
                    params.setTransform(localTransform);
                } else {
                    throw new IllegalArgumentException("Spatial entity transform must be world space");
                }
                EntityBusPeer entityBusPeer = creator.create(params);
                params.setTransform(oldTransform);
                return entityBusPeer;
            }
            case COMPONENT -> {
                throw new IllegalStateException("Components are not supported yet");
            }
            case REFERENCE_OBJECT_DATA -> {
                throw new IllegalStateException("Reference object data are not supported yet");
            }
            default -> {
                return creator.create(params);
            }
        }
    }

    public static class EntityFactoryParams extends EntityCreationInfo {

        @Getter
        private final GameObjectData data;

        public EntityFactoryParams(EntityBus bus, EntityBusPeer.EntityCreatorType creatorType, GameObjectData data) {
            super(bus, creatorType);
            this.data = data;
        }

    }

}
