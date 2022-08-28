package me.battledash.kyber.tests.network;

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import me.battledash.kyber.KyberServer;
import me.battledash.kyber.network.stream.managers.ghost.GhostFactory;
import me.battledash.kyber.runtime.GameTime;
import me.battledash.kyber.server.GameContext;
import me.battledash.kyber.types.network.NetworkableMessageFactory;
import me.battledash.kyber.network.ConnectionManager;
import me.battledash.kyber.network.PacketConnection;
import me.battledash.kyber.network.PacketDecoder;
import me.battledash.kyber.network.PacketEncoder;
import me.battledash.kyber.network.PacketHandler;
import me.battledash.kyber.server.ServerGameContext;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StreamControlTest {

    @Test
    public void testStreamControl() throws DecoderException, IOException {
        ServerGameContext gameContext = ServerGameContext.context();
        gameContext.setServer(new KyberServer());
        gameContext.setGameTime(new GameTime(10));

        GameContext.SIMULATE_CLIENT = true;

        NetworkableMessageFactory.register();
        GhostFactory.register();

        PacketHandler packetHandler = new PacketHandler(new ConnectionManager());

        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline()
                .addLast("decoder", new PacketDecoder())
                .addLast("encoder", new PacketEncoder())
                .addLast(new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors()),
                        "handler", packetHandler);

        packetHandler.setChannel(channel);

        PacketDecoder packetDecoder = new PacketDecoder();
        this.processPacket(packetHandler, packetDecoder, "050064b979b8d2e0c4d70706c00460af2c06068706e7a6a6e5468c2ca62526a66cac25c7ece6a60507c6a6ccc6cca6cc26c766a60f25f2831720000000");
        new ArrayList<>(packetHandler.getConnections()).get(0).setChannelId((short) 1);
        this.processPacket(packetHandler, packetDecoder, "0d004a61b979b892b1e91a230000");

        this.loadPackets(packetHandler, packetDecoder, "packets.txt");
        packetHandler.tick();

        assertTrue(true);
    }

    private void loadPackets(PacketHandler handler, PacketDecoder decoder, String filename) throws IOException, DecoderException {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(filename);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) {
                continue;
            }
            handler.processPacket(decoder.decodePacket(
                    Unpooled.wrappedBuffer(Hex.decodeHex(line)),
                    new InetSocketAddress(25100)
            ));
        }
    }

    private void processPacket(PacketHandler handler, PacketDecoder decoder, String hex) throws DecoderException {
        handler.processPacket(decoder.decodePacket(
                Unpooled.wrappedBuffer(Hex.decodeHex(hex)),
                new InetSocketAddress(25100)
        ));
    }

}
