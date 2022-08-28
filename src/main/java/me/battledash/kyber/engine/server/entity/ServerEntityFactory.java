package me.battledash.kyber.engine.server.entity;

import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.entity.Entity;
import me.battledash.kyber.engine.entity.EntityBus;
import me.battledash.kyber.engine.entity.EntityBusPeer;
import me.battledash.kyber.engine.entity.EntityCreator;
import me.battledash.kyber.engine.entity.EntityFactory;
import me.battledash.kyber.engine.entity.EntityInitInfo;
import me.battledash.kyber.fs.ebx.EbxClassInstance;
import me.battledash.kyber.fs.ebx.EbxClassType;
import me.battledash.kyber.fs.ebx.EbxDataInstance;
import me.battledash.kyber.fs.ebx.EbxReader;
import me.battledash.kyber.fs.ebx.EbxType;
import me.battledash.kyber.types.pojo.Blueprint;
import me.battledash.kyber.types.pojo.GameObjectData;
import me.battledash.kyber.types.pojo.entities.EntityData;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ServerEntityFactory {

    static {
        EntityCreator.activateCreators();
    }

    public static List<Entity> internalCreateFromBlueprint(Void params, EntityBus bus, Blueprint blueprint, EbxType<EbxClassType> blueprintType) {
        if (blueprintType.isKindOf("ObjectBlueprint")) {
            throw new UnsupportedOperationException("ObjectBlueprints are not supported yet");
        } else if (blueprintType.isKindOf("PrefabBlueprint")) {
            return ServerEntityFactory.internalCreateEntities(bus, blueprint.getObjects());
        }
        return null;
    }

    private static List<Entity> internalCreateEntities(EntityBus bus, GameObjectData[] objects) {
        List<Entity> entities = new ArrayList<>();
        for (GameObjectData object : objects) {
            if (!(object instanceof EntityData)) {
                continue;
            }
            log.info("Creating entity of type {}", object.getType());
            EntityBusPeer busPeer = EntityFactory.internalCreateEntity(new EntityFactory.EntityFactoryParams(
                    bus,
                    EntityBusPeer.EntityCreatorType.LEVEL,
                    object
            ));
            if (busPeer instanceof Entity entity) {
                entities.add(entity);
            }
        }
        return entities;
    }

    public static void initCreatedEntities(List<Entity> entities) {
        for (Entity entity : entities) {
            entity.init(new EntityInitInfo());
        }
    }

    public static void initLevel() {
        EntityCreator.initLevel();
    }

}
