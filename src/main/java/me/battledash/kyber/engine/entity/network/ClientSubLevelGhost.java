package me.battledash.kyber.engine.entity.network;

import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.entity.SubLevel;
import me.battledash.kyber.network.stream.managers.ghost.ClientGhost;
import me.battledash.kyber.network.stream.managers.ghost.Ghost;
import me.battledash.kyber.network.stream.managers.ghost.GhostCreationInfo;

@Slf4j
public class ClientSubLevelGhost extends ClientGhost {

    private final SubLevel subLevel;

    public ClientSubLevelGhost(SubLevel subLevel, SubLevelCreationHeader header) {
        this.subLevel = subLevel;
        log.info("Created client sub level ghost for {}", header);
    }

    public static Ghost createClientSubLevelGhost(GhostCreationInfo info) {
        SubLevelCreationHeader header = GhostEntityHelper.readSubLevelCreationHeader(info.getStream(), info.getGhostConnection());

        return new ClientSubLevelGhost(null, header);
    }

    @Override
    public String getNetName() {
        return this.subLevel.getName();
    }

    @Override
    public String getNetClassName() {
        return "ClientSubLevel";
    }

}
