package me.battledash.kyber.server;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.engine.entity.network.ServerSubLevel;
import me.battledash.kyber.engine.player.ServerPlayerManager;
import me.battledash.kyber.engine.server.level.ServerLevel;

@Data
@EqualsAndHashCode(callSuper = true)
public class ServerGameContext extends GameContext {

    private static ServerGameContext INSTANCE;

    public static ServerGameContext context() {
        if (ServerGameContext.INSTANCE == null) {
            ServerGameContext.INSTANCE = new ServerGameContext();
        }
        return ServerGameContext.INSTANCE;
    }

    private ServerLevel serverLevel;
    private ServerSubLevel serverLevelSubLevel;
    private ServerPlayerManager serverPlayerManager = new ServerPlayerManager();

}
