package me.battledash.kyber.engine.player;

import lombok.Data;

@Data
public class ServerPlayer {

    private final int connectionId;
    private final String name;
    private final int localPlayerId;
    private final int playerId;
    private final boolean isSpectator;

}
