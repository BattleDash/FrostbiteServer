package me.battledash.kyber.network.common;

import me.battledash.kyber.network.stream.managers.ghost.GhostConnection;
import me.battledash.kyber.streams.BitStreamRead;

public class ClientVec3ToServerInputExtent extends EntryInputExtent {

    @Override
    public boolean readFromStream(GhostConnection connection, BitStreamRead stream, EntryInputExtent prevState) {
        int i = 0;
        int j = 4;
        do {
            stream.readVector();
            i++;
        } while (i != j);

        return !stream.getStream().isOverflow();
    }

}
