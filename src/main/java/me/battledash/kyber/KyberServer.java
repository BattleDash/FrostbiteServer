package me.battledash.kyber;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.Tickable;
import me.battledash.kyber.engine.entity.DefaultEntityCreator;
import me.battledash.kyber.engine.server.level.ServerLevel;
import me.battledash.kyber.engine.server.level.ServerLevelLoadInfo;
import me.battledash.kyber.engine.simulation.LevelSetup;
import me.battledash.kyber.misc.LoadProgress;
import me.battledash.kyber.network.stream.managers.ghost.GhostFactory;
import me.battledash.kyber.runtime.GameTime;
import me.battledash.kyber.types.network.NetworkableMessageFactory;
import me.battledash.kyber.fs.AssetManager;
import me.battledash.kyber.fs.ResourceManager;
import me.battledash.kyber.fs.ebx.EbxClassInstance;
import me.battledash.kyber.fs.ebx.EbxReader;
import me.battledash.kyber.fs.ebx.EbxTypeLibrary;
import me.battledash.kyber.network.socket.NetworkServer;
import me.battledash.kyber.server.ServerGameContext;
import me.battledash.kyber.util.EnvUtil;
import me.battledash.kyber.util.ProfilingUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Getter
public class KyberServer implements Tickable {

    public static void main(String[] args) {
        new KyberServer().start("0.0.0.0", 25200);
    }

    private final AtomicBoolean started = new AtomicBoolean();

    private NetworkServer networkServer;
    private AssetManager assetManager;
    private GameTime gameTime;
    private ServerLevel level;
    private ServerLevel loadLevel;
    private ServerLevelLoadInfo loadInfo;
    private LevelSetup nextLevelSetup;
    private String activeLevel;

    @Setter
    private int tickRate = 10; // hz

    private boolean loadNextLevelRequested;
    private long totalTicks;
    private int deltaTick;

    public void start(@NonNull SocketAddress address) {
        if (!this.started.compareAndSet(false, true)) {
            throw new IllegalStateException("Server already started");
        }

        try {
            ProfilingUtil.pushTime();
            log.info("Starting Frostbite server");

            ServerGameContext gameContext = ServerGameContext.context();
            gameContext.setServer(this);

            ProfilingUtil.pushTime();
            log.info("Loading game data...");
            String tempPath = "E:\\Program Files (x86)\\Origin Games\\STAR WARS Battlefront II"; // Not final
            this.assetManager = new AssetManager(
                    new ResourceManager(EnvUtil.getProperty("GAME_DATA_DIR", "kyber.datapath", tempPath))
            );
            this.assetManager.init();
            log.info("Game data loaded in {}ms", ProfilingUtil.popAndCalculateTime());

            ProfilingUtil.pushTime();
            this.assetManager.loadIndexCache();
            log.info("EBX indexing done in {}ms", ProfilingUtil.popAndCalculateTime());

            EbxTypeLibrary.initEBXTypes("me.battledash.kyber.types");
            DefaultEntityCreator.registerCreators("me.battledash.kyber.engine.simulation.entities");

            NetworkableMessageFactory.register();
            GhostFactory.register();

            this.gameTime = new GameTime(this.tickRate);
            gameContext.setGameTime(this.gameTime);

            this.loadInfo = new ServerLevelLoadInfo();

            LevelSetup firstLevelSetup = new LevelSetup();
            firstLevelSetup.setName("S5_1/Levels/MP/Geonosis_01/Geonosis_01");
            firstLevelSetup.setInclusionOption("GameMode", "PlanetaryBattles");
            this.nextLevelSetup = firstLevelSetup;

            this.requestStartLoadLevel();

            this.networkServer = new NetworkServer(address);
            this.networkServer.listen();

            log.info("Initial startup completed. Frostbite server listening on {}, startup took {}ms",
                    address, ProfilingUtil.popAndCalculateTime());

            log.info("Initializing Game Loop ({}hz)", this.tickRate);
            while (true) {
                long start = System.currentTimeMillis();
                try {
                    this.tick(this.deltaTick = (int) (this.totalTicks++ % this.tickRate));
                    this.gameTime.setTicks((int) this.totalTicks);
                } catch (Exception e) {
                    log.error("Error while ticking server", e);
                    this.networkServer.stop();
                    System.exit(0);
                }
                long end = System.currentTimeMillis();
                // If the tick took less than the tick rate (tick rate is per seconds), sleep for the difference
                if (end - start < 1000 / this.tickRate) {
                    try {
                        long millis = 1000 / this.tickRate - (end - start);
                        log.debug("Sleeping for {}ms because tick took {}ms ({}ms/tick)", millis, end - start, 1000 / this.tickRate);
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (end - start > 2000 / this.tickRate) {
                    log.warn("Tick took {}ms, which is more than twice the tick rate of {}ms/tick", end - start, 1000 / this.tickRate);
                }
            }
        } catch (Exception e) {
            log.error("Frostbite server failed to start", e);
        }
    }

    public void start(@NonNull String address, int port) {
        this.start(new InetSocketAddress(address, port));
    }

    public void tick(int deltaTick) {
        this.updateLoadLevel();

        this.networkServer.tick(deltaTick);
    }

    public float getSecondsPerTick() {
        return this.tickRate / 1000f;
    }

    public void requestStartLoadLevel() {
        this.loadNextLevelRequested = true;
    }

    private void startLoadLevel() {
        this.activeLevel = this.nextLevelSetup.getName();

        log.info("Starting level load for {} ({})", this.activeLevel, this.nextLevelSetup.getInclusionOptions());

        this.loadLevel = new ServerLevel();

        log.info("ServerLevel initialized");

        this.loadLevel.startLoad(this.loadInfo, this.nextLevelSetup);
    }

    private void updateLoadLevel() {
        if (this.loadNextLevelRequested) {
            this.loadNextLevelRequested = false;
            this.startLoadLevel();
        }

        if (this.loadLevel != null) {
            LoadProgress progress = this.loadLevel.updateLoad(this.loadInfo);

            if (progress == LoadProgress.ABORTED || progress == LoadProgress.FAIL) {
                log.error("Failed to load level {}", this.loadInfo);
                this.loadLevel = null;
                return;
            }

            if (progress == LoadProgress.ACTIVE) {
                return;
            }

            this.finalizeLoadLevel();
        }
    }

    private void finalizeLoadLevel() {
        this.level = this.loadLevel;
        this.loadLevel = null;
    }

    private void ebxDebug() throws IOException {
        ProfilingUtil.pushTime();
        EbxReader.EbxAsset nabooLevel = this.assetManager.getEbxAsset("Levels/MP/Naboo_01/Naboo_01");
        EbxClassInstance levelData = (EbxClassInstance) nabooLevel.getObjects().get(0);
        for (UUID dependency : nabooLevel.getDependencies()) {
            log.info("Dependency: {} ({})", this.assetManager.getEbxEntry(dependency).getName(), dependency);
        }
        log.info("Loaded EBX objects in {}ms, serializing to json", System.currentTimeMillis() - ProfilingUtil.popTime());
        FileUtils.writeStringToFile(new File("ebx.json"), EbxReader.getEbxGson().toJson(levelData), Charset.defaultCharset());
    }

}
