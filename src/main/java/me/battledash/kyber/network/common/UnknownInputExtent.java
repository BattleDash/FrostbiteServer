package me.battledash.kyber.network.common;

import me.battledash.kyber.network.stream.managers.ghost.GhostConnection;
import me.battledash.kyber.streams.BitStreamRead;

public class UnknownInputExtent extends EntryInputExtent {

    @Override
    public boolean readFromStream(GhostConnection connection, BitStreamRead stream, EntryInputExtent prevState) {
        stream.readUnsigned(32);

        return !stream.getStream().isOverflow();
    }

}
