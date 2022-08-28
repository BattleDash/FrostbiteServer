package me.battledash.kyber.network.stream.managers.ghost;

import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.streams.BitStreamWrite;

public interface GhostConnection {

    ClientGhost readGhostId(BitStreamRead stream);

    void writeGhostId(BitStreamWrite stream, ServerGhost ghost);

}
