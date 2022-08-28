package me.battledash.kyber.engine.entity.network;

import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.entity.EntityBus;
import me.battledash.kyber.engine.entity.SubLevel;
import me.battledash.kyber.network.stream.managers.ghost.GhostConnection;
import me.battledash.kyber.network.stream.managers.ghost.ServerGhost;
import me.battledash.kyber.streams.BitStreamWrite;

@Slf4j
public class ServerSubLevelGhost extends ServerGhost {

    private final ServerSubLevel subLevel;
    private final ServerSubLevel parentSubLevel;

    public ServerSubLevelGhost(ServerSubLevel subLevel, ServerSubLevel parentSubLevel) {
        this.subLevel = subLevel;
        this.parentSubLevel = parentSubLevel;

        boolean isBlueprintBundle = subLevel.isBlueprintBundle();
        this.setStaticGhost(!isBlueprintBundle);
        this.setRequiredForLevelSync(!isBlueprintBundle);
        log.info("Created server sub level ghost for {}", subLevel.getName());
    }

    private SubLevelCreationHeader getGhostCreationHeader() {
        SubLevelCreationHeader header = new SubLevelCreationHeader();

        EntityBus rootEntityBus = this.subLevel.getRootEntityBus();

        if (this.subLevel.isBlueprintBundle() || rootEntityBus == null) {
            header.setBus(this.subLevel.getParent().getRootEntityBus());
        } else {
            header.setBus(rootEntityBus.getParentBus());
        }

        header.setSubLevelId(this.subLevel.getSubLevelId());
        header.setUid(this.subLevel.getUniqueId());
        header.setNetworkedBusCount(this.subLevel.getNetworkedBusCount());
        //header.setNetworkedBusCount(10);
        header.setNetworkRegistryHash(0); // Only used in debugging

        if (this.subLevel.getParent() != null) {
            // TODO: 5/10/2022 Set up ServerDestruction
            throw new UnsupportedOperationException("ServerDestruction is not supported yet");
        } else {
            header.setFirstDestructionCallbackId(~0);
        }

        return header;
    }

    @Override
    public void writeNetInit(BitStreamWrite stream, GhostConnection ghostConnection) {
        SubLevelCreationHeader header = this.getGhostCreationHeader();
        log.info("Writing sub level creation header for {}", header);
        GhostEntityHelper.writeSubLevelCreationHeader(stream, ghostConnection, header);
    }

    @Override
    public String getNetName() {
        return this.subLevel.getName();
    }

    @Override
    public String getNetClassName() {
        return "ServerSubLevel";
    }

}
