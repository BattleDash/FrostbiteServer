package me.battledash.kyber.engine.entity.network;

import com.google.common.base.Preconditions;
import me.battledash.kyber.engine.entity.EntityBus;
import me.battledash.kyber.engine.entity.EntityOwner;
import me.battledash.kyber.engine.entity.SubLevel;
import me.battledash.kyber.network.stream.managers.ghost.ServerGhost;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.network.stream.managers.ghost.ClientGhost;
import me.battledash.kyber.network.stream.managers.ghost.GhostConnection;
import me.battledash.kyber.streams.BitStreamWrite;

public class GhostEntityHelper {

    public static EntityOwner readEntityOwner(BitStreamRead stream, GhostConnection ghostConnection, boolean allowNull) {
        ClientGhost ghost = null;
        if (allowNull) {
            ghost = ghostConnection.readGhostId(stream);
        }

        if (ghost != null) {

        }

        return null;
    }

    public static boolean writeEntityOwner(BitStreamWrite stream, GhostConnection ghostConnection, EntityOwner owner, boolean allowNullGhost) {
        Preconditions.checkArgument(owner.isGhost(), "Entity owners written to the network must be ghosts");

        ServerGhost ownerGhost = (ServerGhost) owner.getGhost();
        if (ownerGhost == null && !allowNullGhost) {
            return false;
        }

        ghostConnection.writeGhostId(stream, ownerGhost);

        return true;
    }

    public static EntityBus readEntityBus(BitStreamRead stream, GhostConnection ghostConnection, boolean allowNull) {
        EntityOwner owner = GhostEntityHelper.readEntityOwner(stream, ghostConnection, allowNull);

        long networkId;
        if (stream.readBool()) {
            networkId = stream.readUnsigned(4);
        } else {
            networkId = stream.readUnsigned(16);
        }

        return null;
    }

    public static boolean writeEntityBus(BitStreamWrite stream, GhostConnection ghostConnection, EntityBus bus, boolean allowNullGhost) {
        EntityOwner owner = bus.getOwner();
        if (!GhostEntityHelper.writeEntityOwner(stream, ghostConnection, owner, allowNullGhost)) {
            return false;
        }

        int networkId = bus.getNetworkId();
        if (stream.writeBool(networkId < 16)) {
            stream.writeUnsigned(networkId, 4);
        } else {
            stream.writeUnsigned(networkId, 16);
        }

        return true;
    }

    public static SubLevelCreationHeader readSubLevelCreationHeader(BitStreamRead stream, GhostConnection ghostConnection) {
        SubLevelCreationHeader header = new SubLevelCreationHeader();
        header.setSubLevelId(SubLevel.readSubLevelId(stream));
        header.setUid(stream.readUnsigned(32));

        if (stream.readBool()) {
            header.setBus(GhostEntityHelper.readEntityBus(stream, ghostConnection, true));
        }

        header.setNetworkedBusCount(stream.readUnsigned(32));
        header.setNetworkRegistryHash(stream.readUnsigned(32));

        if (stream.readBool()) {
            header.setFirstDestructionCallbackId(stream.readUnsigned(32));
        } else {
            header.setFirstDestructionCallbackId(~0);
        }

        return header;
    }

    public static void writeSubLevelCreationHeader(BitStreamWrite stream, GhostConnection ghostConnection, SubLevelCreationHeader header) {
        SubLevel.writeSubLevelId(stream, header.getSubLevelId());
        stream.writeUnsigned(header.getUid(), 32);

        if (stream.writeBool(header.getBus() != null)) {
            if (header.getBus().isProxyBus()) {
                GhostEntityHelper.writeEntityBus(stream, ghostConnection, header.getBus().getParentBus(), false);
            } else {
                GhostEntityHelper.writeEntityBus(stream, ghostConnection, header.getBus(), true);
            }
        }

        stream.writeUnsigned(header.getNetworkedBusCount(), 32);
        stream.writeUnsigned(header.getNetworkRegistryHash(), 32);

        if (stream.writeBool(header.getFirstDestructionCallbackId() != ~0)) {
            stream.writeUnsigned(header.getFirstDestructionCallbackId(), 32);
        }
    }

}
