package me.battledash.kyber.server;

import lombok.Data;
import me.battledash.kyber.KyberServer;
import me.battledash.kyber.engine.messages.MessageManager;
import me.battledash.kyber.engine.simulation.level.Level;
import me.battledash.kyber.runtime.GameTime;

@Data
public abstract class GameContext {

    public static boolean SIMULATE_CLIENT = false;
    public static boolean DISABLE_PACKET_TRANSFER = false;

    private MessageManager messageManager = new MessageManager();

    private KyberServer server;
    private GameTime gameTime;
    private Level level;

}
