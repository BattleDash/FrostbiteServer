package me.battledash.kyber.network.common;

import me.battledash.kyber.network.stream.managers.ghost.GhostConnection;
import me.battledash.kyber.streams.BitStreamRead;

public class VehicleEntryInputExtent extends EntryInputExtent {

    @Override
    public boolean readFromStream(GhostConnection connection, BitStreamRead stream, EntryInputExtent prevState) {
        stream.readBool();
        stream.readBool();
        stream.readBool();
        stream.readBool();
        return !stream.getStream().isOverflow();
    }

}
