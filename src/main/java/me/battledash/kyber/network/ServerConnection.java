package me.battledash.kyber.network;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.messages.Listener;
import me.battledash.kyber.engine.messages.Message;
import me.battledash.kyber.engine.player.ServerPlayer;
import me.battledash.kyber.engine.player.ServerPlayerManager;
import me.battledash.kyber.engine.player.messages.PlayerJoinMessage;
import me.battledash.kyber.engine.player.messages.PlayerCreatedForConnectionMessage;
import me.battledash.kyber.network.stream.managers.ghost.ClientGhost;
import me.battledash.kyber.network.stream.managers.ghost.GhostCreationInfo;
import me.battledash.kyber.network.stream.managers.ghost.GhostCreator;
import me.battledash.kyber.network.stream.managers.ghost.GhostFactory;
import me.battledash.kyber.network.stream.managers.ghost.ServerGhost;
import me.battledash.kyber.network.stream.managers.ghost.StreamManagerGhost;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.streams.BitStreamWrite;
import me.battledash.kyber.types.network.messages.NetworkCreatePlayerMessage;
import me.battledash.kyber.types.network.messages.NetworkLevelLoadedAckMessage;
import me.battledash.kyber.types.network.messages.NetworkTinyEventMessage;
import me.battledash.kyber.network.packet.DisconnectReason;
import me.battledash.kyber.network.state.SendStatus;
import me.battledash.kyber.network.state.ServerConnectionState;
import me.battledash.kyber.network.state.ServerFSM;
import me.battledash.kyber.network.stream.managers.ghost.GhostStatusInflicter;
import me.battledash.kyber.network.stream.managers.message.NetworkableMessage;
import me.battledash.kyber.server.ServerGameContext;

import java.net.InetSocketAddress;

@Slf4j
@Getter
public class ServerConnection extends PacketConnection implements Listener, GhostStatusInflicter, GhostCreator {

    private final StateFlowFlags stateFlowFlags = new StateFlowFlags();

    private final ServerFSM stateMachine;

    private DisconnectReason disconnectReason;
    private String disconnectMessage;

    private ServerPlayer player;

    public ServerConnection(PacketHandler handler, InetSocketAddress address, int connectionId) {
        super(handler, address, connectionId);
        this.stateMachine = new ServerFSM(this);

        ServerGameContext.context().getMessageManager().registerListener(this);
    }

    @Override
    public void init() {
        super.init();

        StreamManagerGhost ghostManager = this.getGhostManager();
        ghostManager.setCreator(this);
        ghostManager.setStatusInflicter(this);
    }

    @Override
    public void disconnected() {
        ServerGameContext context = ServerGameContext.context();

        context.getServerPlayerManager().removePlayer(this.getPlayer());
        context.getMessageManager().unregisterListener(this);
        this.handler.getConnections().remove(this);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.disconnectReason != null) {
            this.unsafeDisconnect(this.disconnectReason, this.disconnectMessage);
        }

        this.stateMachine.tick();
    }

    @Override
    public void onConnected() {
        log.debug("Connected to {}", this.getAddress());

        this.stateMachine.setState(new ServerConnectionState.StartTimeSyncState());
    }

    public void sendTinyEvent(TinyEvent event) {
        NetworkTinyEventMessage message = new NetworkTinyEventMessage();
        message.setTinyEvent(event);

        this.sendMessageUnchecked(message);
    }

    private void onCreatePlayer(NetworkCreatePlayerMessage message) {
        ServerPlayerManager playerManager = ServerGameContext.context().getServerPlayerManager();

        String name = message.getName();

        if (name.isEmpty()) {
            name = "Player" + this.getChannelId();
        }

        int connectionId = this.getConnectionId();
        Preconditions.checkState(connectionId < 256, "Connection id is too large");

        boolean spectate = message.isSpectator();
        // TODO: 5/9/2022 Handle spectating logic

        this.player = playerManager.createPlayer(connectionId, name, 0, ~0, spectate);

        new PlayerCreatedForConnectionMessage(this, this.player).callMessage();

        if (!new PlayerJoinMessage(this.player).callMessage()) {
            this.kick(DisconnectReason.KICKED_OUT, "");
        }

        log.info("{}[{}] logged in with connection id {}",
                this.player.getName(), this.getAddress(), this.getConnectionId());
    }

    private void onTinyEvent(TinyEvent tinyEvent) {
        switch (tinyEvent) {
            case ACK_TIME_SYNC_DONE -> this.stateFlowFlags.setAckTimeSyncDone(true);
            case ACK_LEVEL_LINKED -> this.stateFlowFlags.setAckLevelLinked(true);
            default -> log.warn("Unknown tiny event: {}", tinyEvent);
        }
    }

    @Override
    public void onMessage(Message message) {
        if (message instanceof NetworkableMessage) {
            if (message instanceof NetworkCreatePlayerMessage createPlayerMessage) {
                this.onCreatePlayer(createPlayerMessage);
            } else if (message instanceof NetworkTinyEventMessage tinyMessage) {
                this.onTinyEvent(tinyMessage.getTinyEvent());
            } else if (message instanceof NetworkLevelLoadedAckMessage) {
                this.stateFlowFlags.setAckLevelLoaded(true);
            }
        }
    }

    /**
     * Kicks the player with the given reason and message
     *
     * @param reason The reason for the kick
     * @param message The message to send to the player
     */
    public void kick(DisconnectReason reason, String message) {
        this.disconnectReason = reason;
        this.disconnectMessage = message;
    }

    /**
     * This method is not thread safe! Use {@link ServerConnection#kick(DisconnectReason, String)}
     *
     * @param reason The reason for the disconnect
     * @param message The message to send to the client
     */
    public void unsafeDisconnect(DisconnectReason reason, String message) {
        this.sendDisconnect(reason, message);
        this.disconnected();
    }

    @Override
    public SendStatus getSendStatus() {
        ServerConnectionState state = this.stateMachine.getCurrentState();
        if (state != null) {
            return state.getSendStatus();
        } else {
            return SendStatus.DO_NOT_SEND;
        }
    }

    @Override
    public boolean createGhost(BitStreamRead stream, int id, GhostCreatorResult result) {
        int classId = (int) stream.readUnsigned(6);
        long something = stream.readUnsigned(32);

        log.info("Creating new ghost with id {} (class id {}, {})", id, classId, something);
        GhostCreationInfo info = new GhostCreationInfo(stream, this.getGhostConnection());

        ClientGhost ghost = GhostFactory.createGhost(classId, info);
        if (ghost == null) {
            return false;
        }

        result.setGhost(ghost);

        return true;
    }

    @Override
    public void writeGhostHeader(BitStreamWrite stream, ServerGhost ghost) {
        int ghostType = ghost.getNetClassId();
        stream.writeUnsigned(ghostType, 6);
        stream.writeUnsigned(2885258064L, 32); // TODO: 5/10/2022 Figure out what this is
        ghost.writeNetInit(stream, this.getGhostConnection());
    }

    @Data
    public static class StateFlowFlags {
        private boolean ackLevelLoaded;
        private boolean ackLevelLinked;
        private boolean ackEnterPatchRecvState;
        private boolean ackTimeSyncDone;
        private boolean ackExitLevel;
        private boolean pendingLeveLoad;
    }

}
