package me.battledash.kyber.engine.player;

import java.util.ArrayList;
import java.util.List;

public class ServerPlayerManager {

    private final List<ServerPlayer> players = new ArrayList<>();
    private final List<ServerPlayer> spectators = new ArrayList<>();

    public ServerPlayer createPlayer(int connectionId, String name, int localPlayerId, int playerId, boolean isSpectator) {
        ServerPlayer player = new ServerPlayer(connectionId, name, localPlayerId, playerId, isSpectator);
        this.addPlayer(player);
        return player;
    }

    public void removePlayer(ServerPlayer player) {
        this.players.remove(player);
        this.spectators.remove(player);
    }

    public void addPlayer(ServerPlayer player) {
        // TODO: 5/9/2022 Register player as a ghost

        if (player.isSpectator()) {
            this.spectators.add(player);
        } else {
            this.players.add(player);
        }
    }

}
