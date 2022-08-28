package me.battledash.kyber.engine.simulation.entities.world;

import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.entity.EntityCreationInfo;
import me.battledash.kyber.engine.entity.EntityEvent;
import me.battledash.kyber.engine.entity.EntityWithBusAndData;
import me.battledash.kyber.engine.entity.FrostbiteEntity;
import me.battledash.kyber.types.pojo.entities.TerrainEntityData;

@Slf4j
@FrostbiteEntity(TerrainEntityData.class)
public class TerrainEntity extends EntityWithBusAndData<TerrainEntityData> {

    public TerrainEntity(EntityCreationInfo info, TerrainEntityData data) {
        super(info, data);

        TerrainEntityData.TerrainData terrainData = data.getTerrainAsset();
        boolean isModifierDatabaseRequired = terrainData.isDynamicMaskEnable();
        // TODO: 4/15/2022 Create terrain
        info.getTransform().getTrans().set(0, 0, 0);
    }

    @Override
    public void onCreate(EntityCreationInfo info) {
    }

    @Override
    public void event(EntityEvent event) {
    }

}