package me.battledash.kyber.network;

import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.streams.InBitStream;

import java.net.InetSocketAddress;

@Slf4j
public class ConnectionManager {

    public void onConnectionRequest(PacketHandler packetHandler, InetSocketAddress address, long clientNonce, byte[] info) {
        InBitStream stream = new InBitStream();
        stream.initBits(Unpooled.wrappedBuffer(info), info.length * 8);

        String machineId = stream.readString();
        // TODO: 4/3/2022 Check machine ban
        long sessionId = stream.read(4);
        byte[] liveEditing = stream.readOctets(1);

        long validLocalPlayersMask = stream.read(16);
        int primaryLocalPlayerId = (int) stream.read(8);

        log.info("Connection request from {}: machineId={}, sessionId={}, liveEditing={}, validLocalPlayersMask={}, primaryLocalPlayerId={}",
                clientNonce, machineId, sessionId, liveEditing, validLocalPlayersMask, primaryLocalPlayerId);

        // TODO: 4/3/2022 Verify server isn't full

        packetHandler.acceptConnection(address, validLocalPlayersMask, clientNonce);
    }

}
