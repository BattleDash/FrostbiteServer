package me.battledash.kyber.network.common;

import me.battledash.kyber.network.stream.managers.ghost.GhostConnection;
import me.battledash.kyber.streams.BitStreamRead;

public abstract class EntryInputExtent {

    public abstract boolean readFromStream(GhostConnection connection, BitStreamRead stream, EntryInputExtent prevState);

}
