package me.battledash.kyber.network.stream.managers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.network.stream.StreamManager;
import me.battledash.kyber.streams.BitStreamRead;

@Slf4j
public class StreamManagerChat extends StreamManager {

    @Getter
    private final String name = "Chat";

    @Override
    public boolean processReceivedPacket(BitStreamRead stream) {
        long i = stream.readUnsignedLimit(0, 1);
        long j = stream.readUnsigned64(64);
        long h = stream.readUnsignedLimit(0, 16);
        long g = stream.readUnsigned(3);
        String s = stream.readString();
        log.info("Received packet with i {} j {} h {} g {} s {}", i, j, h, g, s);
        return true;
    }

}
