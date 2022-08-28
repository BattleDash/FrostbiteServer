package me.battledash.kyber.engine.server.level;

import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.entity.Entity;
import me.battledash.kyber.engine.entity.EntityBus;
import me.battledash.kyber.engine.entity.network.ServerSubLevel;
import me.battledash.kyber.engine.server.entity.ServerEntityFactory;
import me.battledash.kyber.engine.simulation.LevelSetup;
import me.battledash.kyber.engine.simulation.level.Level;
import me.battledash.kyber.misc.LoadProgress;
import me.battledash.kyber.server.ServerGameContext;

import java.util.List;

@Slf4j
public class ServerLevel extends Level {

    private final ServerSubLevel subLevel;

    public ServerLevel() {
        this.subLevel = new ServerSubLevel(this.getLevelUid(), null, null);
    }

    public void startLoad(ServerLevelLoadInfo loadInfo, LevelSetup setup) {
        this.setup = setup;

        ServerGameContext.context().setLevel(this);
        ServerGameContext.context().setServerLevel(this);
        ServerGameContext.context().setServerLevelSubLevel(this.subLevel);

        loadInfo.setState(ServerLoadState.LOADING);
    }

    public LoadProgress updateLoad(ServerLevelLoadInfo loadInfo) {
        LoadProgress progress = LoadProgress.ACTIVE;
        log.info("Loading level... {}", loadInfo.getState());
        switch (loadInfo.getState()) {
            case LOADING -> {
                LoadProgress loadResourceProgress = this.updateLoadLevelResources(this.subLevel, loadInfo);
                if (loadResourceProgress != LoadProgress.SUCCESS) {
                    return loadResourceProgress;
                }
                loadInfo.setState(ServerLoadState.INITIALIZING);
            }
            case INITIALIZING -> {
                if (!this.initialize(loadInfo)) {
                    progress = LoadProgress.FAIL;
                    break;
                }
                loadInfo.setState(ServerLoadState.WAITING_FOR_SUBLEVEL_DATA);
            }
            case WAITING_FOR_SUBLEVEL_DATA -> {
                loadInfo.setState(ServerLoadState.SPAWNING_ENTITIES);
            }
            case SPAWNING_ENTITIES -> {
                this.initializeEntities(this.spawn(loadInfo));
                loadInfo.setState(ServerLoadState.FINALIZING);
            }
            case FINALIZING -> {
                loadInfo.setState(ServerLoadState.DONE);
                progress = LoadProgress.SUCCESS;
                log.info("------------ Server level '{}' loaded -------------", this.setup.getName());
            }
        }
        return progress;
    }

    private boolean initialize(ServerLevelLoadInfo loadInfo) {
        ServerGameContext gameContext = ServerGameContext.context();

        // TODO: 4/13/2022 Update load pathfinding

        // TODO: 4/13/2022 Load dedicated server terrain resources

        ServerSubLevelManager.getManager().startLevelLoad(this.subLevel, this.data);

        this.entityBus = EntityBus.createRootBus(this.subLevel, this.data);

        if (this.data.getNeedNetworkId()) {
            this.subLevel.registerEntityBus(this.entityBus);
        }

        this.subLevel.setRootEntityBus(this.entityBus);

        return true;
    }

    private List<Entity> spawn(ServerLevelLoadInfo loadInfo) {
        ServerEntityFactory.initLevel();

        // TODO: 4/13/2022 Init aiSystem and ai2System (LicenseeServerLevel::internalLevelSpawningBegin)

        return this.spawnEntities(loadInfo);
    }

    private List<Entity> spawnEntities(ServerLevelLoadInfo loadInfo) {
        ServerSubLevelManager subLevelManager = ServerSubLevelManager.getManager();

        return subLevelManager.spawnLevelEntities();
    }

    private void initializeEntities(List<Entity> entities) {
        ServerEntityFactory.initCreatedEntities(entities);
    }

}
