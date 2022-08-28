package me.battledash.kyber.engine.server.level;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.entity.Entity;
import me.battledash.kyber.engine.entity.EntityBus;
import me.battledash.kyber.engine.entity.SubLevel;
import me.battledash.kyber.engine.entity.network.ServerSubLevel;
import me.battledash.kyber.engine.messages.Listener;
import me.battledash.kyber.engine.messages.Message;
import me.battledash.kyber.engine.server.entity.ServerEntityFactory;
import me.battledash.kyber.types.network.messages.SubLevelFromClientRequestBundleBaselineMessage;
import me.battledash.kyber.types.network.messages.SubLevelToClientDropBundleBaselineMessage;
import me.battledash.kyber.types.network.messages.SubLevelToClientLoadRequestsMessage;
import me.battledash.kyber.types.network.messages.SubLevelToClientSubLevelNameMessage;
import me.battledash.kyber.fs.ebx.EbxTypeLibrary;
import me.battledash.kyber.network.ServerConnection;
import me.battledash.kyber.network.stream.managers.message.NetworkableMessage;
import me.battledash.kyber.server.ServerGameContext;
import me.battledash.kyber.types.pojo.Blueprint;
import me.battledash.kyber.types.pojo.level.BundleHeapInfo;
import me.battledash.kyber.types.pojo.level.LevelData;

import java.util.List;

@Data
@Slf4j
public class ServerSubLevelManager implements Listener {

    @Getter
    private static final ServerSubLevelManager manager = new ServerSubLevelManager();

    private SubLevelRef mainLevelSubRef;

    public ServerSubLevelManager() {
        ServerGameContext.context().getMessageManager().registerListener(this);
    }

    public List<Entity> spawnLevelEntities() {
        ServerSubLevel level = ServerGameContext.context().getServerLevelSubLevel();
        EntityBus bus = level.getRootEntityBus();
        Blueprint levelData = level.getSubLevelData();

        return ServerEntityFactory.internalCreateFromBlueprint(null, bus, levelData, EbxTypeLibrary.getType("LevelData"));
    }

    @Override
    public void onMessage(Message message) {
        if (message instanceof NetworkableMessage networkableMessage) {
            if (networkableMessage instanceof SubLevelFromClientRequestBundleBaselineMessage receivedMsg) {
                if (true || receivedMsg.getLevelSequenceNumber() != 1) {
                    SubLevelToClientDropBundleBaselineMessage dropBaseLineMsg = new SubLevelToClientDropBundleBaselineMessage();
                    dropBaseLineMsg.setLevelSequenceNumber(receivedMsg.getLevelSequenceNumber());
                    this.sendNetworkedMessage(dropBaseLineMsg, receivedMsg.getServerConnection());
                }
                this.sendBaseline(receivedMsg.getServerConnection());
                return;
            }
        }
    }

    public void startLevelLoad(SubLevel level, LevelData levelData) {
        level.setSubLevelId(0);

        this.mainLevelSubRef = new SubLevelRef(level.getSubLevelId());

        ServerGameContext.context().getServer().getAssetManager().registerString(levelData.getName());

        log.info("Starting level load for level {}, subLevel id {}", levelData.getName(), level.getSubLevelId());
    }

    public void updateBundleNameToIndex(ServerConnection connection) {
        SubLevelToClientSubLevelNameMessage msg = new SubLevelToClientSubLevelNameMessage();
        msg.setLevelSequenceNumber(1);

        List<SubLevelToClientSubLevelNameMessage.BundleNameAndIndex> bundles = msg.getBundles();
        bundles.add(new SubLevelToClientSubLevelNameMessage.BundleNameAndIndex(0, "S5_1/Levels/MP/Geonosis_01/Lobby"));
        bundles.add(new SubLevelToClientSubLevelNameMessage.BundleNameAndIndex(1, "S5_1/Levels/MP/Geonosis_01/World_Content"));
        bundles.add(new SubLevelToClientSubLevelNameMessage.BundleNameAndIndex(2, "S5_1/Levels/MP/Geonosis_01/Cinematics"));
        bundles.add(new SubLevelToClientSubLevelNameMessage.BundleNameAndIndex(3, "S5_1/Levels/MP/Geonosis_01/GameModes"));
        bundles.add(new SubLevelToClientSubLevelNameMessage.BundleNameAndIndex(4, "S5_1/Levels/MP/Geonosis_01/FantasyBattle"));

        this.sendNetworkedMessage(msg, connection);
    }

    public void sendLoaded(Object handles, ServerConnection connection, boolean isBaseline) {
        this.updateBundleNameToIndex(connection);

        SubLevelToClientLoadRequestsMessage msg = new SubLevelToClientLoadRequestsMessage();
        List<SubLevelToClientLoadRequestsMessage.SubLevelBundleInfo> infos = msg.getInfos();

        infos.add(new SubLevelToClientLoadRequestsMessage.SubLevelBundleInfo(
                0, 1, 0, 18872717L, 5, (short) 254, false, false,
                new BundleHeapInfo(BundleHeapInfo.BundleHeapType.PARENT, 0, true),
                SubLevelToClientLoadRequestsMessage.BundleType.SUB_LEVEL,
                new SubLevelToClientLoadRequestsMessage.BundleSettingsInfo(0, "")
        ));

        infos.add(new SubLevelToClientLoadRequestsMessage.SubLevelBundleInfo(
                1, 2, 0, 17293478L, 6, (short) 254, false, false,
                new BundleHeapInfo(BundleHeapInfo.BundleHeapType.PARENT, 0, true),
                SubLevelToClientLoadRequestsMessage.BundleType.SUB_LEVEL,
                new SubLevelToClientLoadRequestsMessage.BundleSettingsInfo(0, "")
        ));

        infos.add(new SubLevelToClientLoadRequestsMessage.SubLevelBundleInfo(
                1, 3, 0, 402092L, 7, (short) 254, false, false,
                new BundleHeapInfo(BundleHeapInfo.BundleHeapType.PARENT, 0, true),
                SubLevelToClientLoadRequestsMessage.BundleType.SUB_LEVEL,
                new SubLevelToClientLoadRequestsMessage.BundleSettingsInfo(0, "")
        ));

        infos.add(new SubLevelToClientLoadRequestsMessage.SubLevelBundleInfo(
                3, 4, 0, 29890759L, 8, (short) 254, false, false,
                new BundleHeapInfo(BundleHeapInfo.BundleHeapType.PARENT, 0, true),
                SubLevelToClientLoadRequestsMessage.BundleType.SUB_LEVEL,
                new SubLevelToClientLoadRequestsMessage.BundleSettingsInfo(0, "")
        ));

        infos.add(new SubLevelToClientLoadRequestsMessage.SubLevelBundleInfo(
                4, 5, 4, 24687127L, 9, (short) 254, false, false,
                new BundleHeapInfo(BundleHeapInfo.BundleHeapType.PARENT, 0, true),
                SubLevelToClientLoadRequestsMessage.BundleType.SUB_LEVEL,
                new SubLevelToClientLoadRequestsMessage.BundleSettingsInfo(0, "")
        ));

        msg.setBaselineMessage(isBaseline);
        msg.setLevelSequenceNumber(1);
        this.sendNetworkedMessage(msg, connection);
    }

    public void sendBaseline(ServerConnection connection) {
        this.sendLoaded(null, connection, true);
    }

    private void sendNetworkedMessage(final NetworkableMessage message, ServerConnection connection) {
        if (connection != null) {
            connection.sendMessageUnchecked(message);
        }
    }

}
