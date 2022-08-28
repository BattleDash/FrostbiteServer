package me.battledash.kyber.network.stream.managers.ghost;

import me.battledash.kyber.streams.BitStreamWrite;

public abstract class ServerGhost extends Ghost {

    public abstract void writeNetInit(BitStreamWrite stream, GhostConnection connection);

    public void writeNetworkables(BitStreamWrite stream, GhostConnection connection) {

    }

}
