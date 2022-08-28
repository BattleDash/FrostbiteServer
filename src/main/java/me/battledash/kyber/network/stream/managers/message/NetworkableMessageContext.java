package me.battledash.kyber.network.stream.managers.message;

import lombok.Data;
import me.battledash.kyber.network.PacketConnection;
import me.battledash.kyber.network.ServerConnection;
import me.battledash.kyber.network.stream.managers.ghost.GhostConnection;

@Data
public class NetworkableMessageContext {

    private final PacketConnection serverConnection;
    private final GhostConnection ghostConnection;

    public void apply(NetworkableMessage message) {
        message.setServerConnection((ServerConnection) this.serverConnection);
        message.setGhostConnection(this.ghostConnection);
    }

}
