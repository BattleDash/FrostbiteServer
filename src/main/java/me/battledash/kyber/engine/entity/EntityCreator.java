package me.battledash.kyber.engine.entity;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import me.battledash.kyber.types.pojo.ComponentData;
import me.battledash.kyber.types.pojo.GameObjectData;
import me.battledash.kyber.types.pojo.ReferenceObjectData;
import me.battledash.kyber.types.pojo.entities.EntityData;
import me.battledash.kyber.types.pojo.entities.SpatialEntityData;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public abstract class EntityCreator {

    private static final Map<Type, EntityCreator> CREATOR_REGISTRY = new LinkedHashMap<>();

    public static EntityCreator getCreator(Type dataType) {
        return EntityCreator.CREATOR_REGISTRY.get(dataType);
    }

    public static EntityCreator setCreator(Type dataType, EntityCreator creator) {
        EntityCreator oldCreator = EntityCreator.getCreator(dataType);
        EntityCreator.CREATOR_REGISTRY.put(dataType, creator);
        return oldCreator;
    }

    @Getter
    @Setter
    private static EntityCreator firstCreator = null;

    private EntityCreator previousCreator;
    private EntityCreator nextCreator;
    private boolean linked;
    private boolean active;
    private boolean initialized;
    private boolean bound;

    private Class<? extends EntityBusPeer> entityClass;
    private Class<? extends EntityData> dataClass;
    private SpecialType specialType;

    public EntityCreator(Class<? extends EntityBusPeer> entityClass, Class<? extends EntityData> dataClass, boolean link) {
        this.entityClass = entityClass;
        this.dataClass = dataClass;

        if (link) {
            this.link();
        }
    }

    public void onInitLevel() {

    }

    public void link() {
        if (this.linked) {
            return;
        }

        EntityCreator firstCreator = EntityCreator.getFirstCreator();
        if (firstCreator != null) {
            firstCreator.setPreviousCreator(this);
        }
        this.nextCreator = firstCreator;
        this.previousCreator = null;
        EntityCreator.setFirstCreator(this);
        this.linked = true;
    }

    public void bind() {

    }

    public abstract EntityCreator registerToData();

    public abstract EntityBusPeer create(EntityFactory.EntityFactoryParams params);

    public static void activateCreators() {
        EntityCreator creator = EntityCreator.getFirstCreator();
        while (creator != null) {
            EntityCreator oldCreator = creator.activate();

            if (oldCreator != null) {
                // TODO: 4/15/2022 Handle creator priority
            }

            creator = creator.getNextCreator();
        }
    }

    public EntityCreator activate() {
        if (this.active) {
            return this;
        }

        if (!this.initialized) {
            Preconditions.checkState(GameObjectData.class.isAssignableFrom(this.dataClass),
                    "EntityCreator data type must be extending from GameObjectData (this is " + this.dataClass.getSimpleName() + ")");

            if (SpatialEntityData.class.isAssignableFrom(this.dataClass)) {
                this.specialType = SpecialType.SPATIAL_ENTITY;
            } else if (ComponentData.class.isAssignableFrom(this.dataClass)) {
                this.specialType = SpecialType.COMPONENT;
            } else if (ReferenceObjectData.class.isAssignableFrom(this.dataClass)) {
                this.specialType = SpecialType.REFERENCE_OBJECT_DATA;
            } else {
                this.specialType = SpecialType.NONE;
            }

            this.initialized = true;
        }

        this.active = true;

        EntityCreator oldCreator = this.registerToData();
        if (oldCreator != null) {
            oldCreator.setActive(false);
        }
        return oldCreator;
    }

    public static void initLevel() {
        EntityCreator creator = EntityCreator.getFirstCreator();
        while (creator != null) {
            creator.onInitLevel();
            creator = creator.getNextCreator();
        }
    }

    public enum SpecialType {
        NONE,
        SPATIAL_ENTITY,
        COMPONENT,
        REFERENCE_OBJECT_DATA
    }

}
