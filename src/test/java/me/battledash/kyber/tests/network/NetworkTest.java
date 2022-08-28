package me.battledash.kyber.tests.network;

import me.battledash.kyber.KyberServer;
import me.battledash.kyber.engine.server.level.ServerSubLevelManager;
import me.battledash.kyber.runtime.GameTime;
import me.battledash.kyber.types.network.NetworkableMessageFactory;
import me.battledash.kyber.network.socket.NetworkServer;
import me.battledash.kyber.server.ServerGameContext;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

public class NetworkTest {

    @Test
    public void testNetwork() {
        ServerGameContext gameContext = ServerGameContext.context();
        gameContext.setServer(new KyberServer());
        gameContext.setGameTime(new GameTime(10));

        ServerSubLevelManager.getManager();

        NetworkServer networkServer = new NetworkServer(new InetSocketAddress("0.0.0.0", 25200));
        networkServer.listen();

        NetworkableMessageFactory.register();

        System.out.println("Server started");

        while (true) {
            long start = System.currentTimeMillis();
            networkServer.tick(0);
            long end = System.currentTimeMillis();
            try {
                long millis = 1000 / 10 - (end - start);
                Thread.sleep(Math.max(0, millis));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


}
