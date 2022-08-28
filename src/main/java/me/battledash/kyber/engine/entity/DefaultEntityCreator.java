package me.battledash.kyber.engine.entity;

import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.simulation.entities.compare.CompareBoolEntity;
import me.battledash.kyber.engine.simulation.entities.debug.PrintDebugTextEntity;
import me.battledash.kyber.engine.simulation.entities.logic.AndEntity;
import me.battledash.kyber.engine.simulation.entities.misc.ConsoleCommandTriggerEntity;
import me.battledash.kyber.engine.simulation.entities.misc.DelayEntity;
import me.battledash.kyber.engine.simulation.entities.misc.InclusionSettingEntity;
import me.battledash.kyber.engine.simulation.entities.player.LocalPlayerEntity;
import me.battledash.kyber.engine.simulation.entities.sound.IrReverbEntity;
import me.battledash.kyber.engine.simulation.entities.sound.MixerEntity;
import me.battledash.kyber.engine.simulation.entities.synced.SyncedBoolEntity;
import me.battledash.kyber.engine.simulation.entities.world.PathfindingStreamEntity;
import me.battledash.kyber.engine.simulation.entities.world.SchematicChannelEntity;
import me.battledash.kyber.engine.simulation.entities.world.ServerStaticModelGroupEntity;
import me.battledash.kyber.engine.simulation.entities.world.WaterEntity;
import me.battledash.kyber.engine.simulation.entities.sound.DiceSoundAreaEntity;
import me.battledash.kyber.engine.simulation.entities.world.PathfindingSystemEntity;
import me.battledash.kyber.engine.simulation.entities.world.ShadowExtrusionDataEntity;
import me.battledash.kyber.engine.simulation.entities.world.ShadowExtrusionLevelDataEntity;
import me.battledash.kyber.engine.simulation.entities.world.TerrainEntity;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.fs.ebx.EbxReader;
import me.battledash.kyber.types.pojo.entities.EntityData;
import me.battledash.kyber.util.ClassUtils;

import java.lang.reflect.InvocationTargetException;

@Slf4j
public class DefaultEntityCreator extends EntityCreator {

    public DefaultEntityCreator(Class<? extends EntityBusPeer> entityClass, Class<? extends EntityData> dataClass, boolean link) {
        super(entityClass, dataClass, link);
    }

    @Override
    public EntityCreator registerToData() {
        return EntityCreator.setCreator(this.getDataClass(), this);
    }

    @Override
    public EntityBusPeer create(EntityFactory.EntityFactoryParams params) {
        try {
            EntityBusPeer entityBusPeer = this.getEntityClass()
                    .getConstructor(EntityCreationInfo.class, this.getDataClass())
                    .newInstance(params, params.getData());
            entityBusPeer.create(params);
            return entityBusPeer;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Failed to create entity", e);
        }
        return null;
    }

    public static EntityCreator registerCreator(Class<? extends EntityBusPeer> entityClass) {
        Class<? extends EntityData> dataClass = entityClass.getAnnotation(FrostbiteEntity.class).value();
        DefaultEntityCreator creator = new DefaultEntityCreator(entityClass, dataClass, true);
        EntityCreator.setCreator(dataClass, creator);
        return creator;
    }

    public static void registerCreators(String packageName) {
        for (Class<?> clazz : ClassUtils.findAllClasses(packageName)) {
            if (EntityBusPeer.class.isAssignableFrom(clazz)) {
                DefaultEntityCreator.registerCreator((Class<? extends EntityBusPeer>) clazz);
            }
        }
        /*Class<?>[] entityClasses = {
                ServerStaticModelGroupEntity.class,
                TerrainEntity.class,
                ShadowExtrusionLevelDataEntity.class,
                ShadowExtrusionDataEntity.class,
                PathfindingSystemEntity.class,
                PathfindingStreamEntity.class,
                WaterEntity.class,
                DiceSoundAreaEntity.class,
                CompareBoolEntity.class,
                PrintDebugTextEntity.class,
                SyncedBoolEntity.class,
                LocalPlayerEntity.class,
                SchematicChannelEntity.class,
                MixerEntity.class,
                DelayEntity.class,
                ConsoleCommandTriggerEntity.class,
                InclusionSettingEntity.class,
                AndEntity.class,
                IrReverbEntity.class
        };

        for (Class<?> entityClass : entityClasses) {
            DefaultEntityCreator.registerCreator((Class<? extends EntityBusPeer>) entityClass);
        }*/
    }

}
