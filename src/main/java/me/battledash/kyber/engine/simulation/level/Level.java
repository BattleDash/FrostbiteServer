package me.battledash.kyber.engine.simulation.level;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.entity.EntityBus;
import me.battledash.kyber.engine.entity.SimpleEntityOwner;
import me.battledash.kyber.engine.entity.SubLevel;
import me.battledash.kyber.engine.server.level.ServerLevelLoadInfo;
import me.battledash.kyber.engine.simulation.LevelSetup;
import me.battledash.kyber.misc.LoadProgress;
import me.battledash.kyber.fs.AssetManager;
import me.battledash.kyber.fs.ebx.EbxClassInstance;
import me.battledash.kyber.server.ServerGameContext;
import me.battledash.kyber.types.pojo.Blueprint;
import me.battledash.kyber.types.pojo.level.LevelData;

import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public class Level {

    protected List<AssetManager.EbxAssetEntry> assets = new ArrayList<>();
    protected LevelSetup setup;
    protected LevelData data;
    protected EntityBus entityBus;

    public int getLevelUid() {
        return 1;
    }

    public LoadProgress updateLoadLevelResources(SubLevel level, ServerLevelLoadInfo loadInfo) {
        this.data = ServerGameContext.context()
                .getServer()
                .getAssetManager()
                .getEbxAsset(this.setup.getName())
                .<EbxClassInstance>getObjectOfType("LevelData")
                .convertToPOJO();
        level.setSubLevelData(this.data);

        // TODO: 4/13/2022 onLevelDataChanged (sets up unlockManager)

        log.info("Resources for level '{}' loaded", this.setup.getName());
        return LoadProgress.SUCCESS;
    }

}
