package me.battledash.kyber.network.stream.managers;

import lombok.Getter;
import me.battledash.kyber.network.stream.StreamManager;

public class StreamManagerDummy extends StreamManager {

    @Getter
    private final String name;

    public StreamManagerDummy(String name) {
        this.name = name;
        this.setDebugBroadcastProcessing(true);
    }

}
