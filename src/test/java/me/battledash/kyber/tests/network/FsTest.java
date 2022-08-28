package me.battledash.kyber.tests.network;

import me.battledash.kyber.KyberServer;
import me.battledash.kyber.fs.AssetManager;
import me.battledash.kyber.fs.ResourceManager;
import me.battledash.kyber.fs.ebx.EbxClassInstance;
import me.battledash.kyber.fs.ebx.EbxReader;
import me.battledash.kyber.fs.ebx.EbxTypeLibrary;
import me.battledash.kyber.server.ServerGameContext;
import me.battledash.kyber.util.EnvUtil;
import me.battledash.kyber.util.ProfilingUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;

public class FsTest {

    @Test
    public void testNetwork() throws IOException {
        ServerGameContext gameContext = ServerGameContext.context();
        gameContext.setServer(new KyberServer());

        ProfilingUtil.pushTime();
        System.out.println("Loading game data...");
        String tempPath = "E:\\Program Files (x86)\\Origin Games\\STAR WARS Battlefront II";
        AssetManager assetManager = new AssetManager(
                new ResourceManager(EnvUtil.getProperty("GAME_DATA_DIR", "kyber.datapath", tempPath))
        );
        assetManager.init();
        System.out.println("Game data loaded in " + ProfilingUtil.popAndCalculateTime() + "ms");

        ProfilingUtil.pushTime();
        assetManager.loadIndexCache();
        System.out.println("EBX indexing done in " + ProfilingUtil.popAndCalculateTime() + "ms");

        EbxTypeLibrary.initEBXTypes("me.battledash.kyber.types");

        EbxReader.EbxAsset nabooLevel = assetManager.getEbxAsset("Levels/MP/Naboo_01/Naboo_01");
        EbxClassInstance levelData = (EbxClassInstance) nabooLevel.getObjects().get(0);
        FileUtils.writeStringToFile(new File("ebx.json"), EbxReader.getEbxGson().toJson(levelData), Charset.defaultCharset());
    }


}
