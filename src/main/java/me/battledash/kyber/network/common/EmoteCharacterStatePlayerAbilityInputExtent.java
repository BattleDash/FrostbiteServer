package me.battledash.kyber.network.common;

import me.battledash.kyber.network.stream.managers.ghost.GhostConnection;
import me.battledash.kyber.streams.BitStreamRead;

public class EmoteCharacterStatePlayerAbilityInputExtent extends EntryInputExtent {

    @Override
    public boolean readFromStream(GhostConnection connection, BitStreamRead stream, EntryInputExtent prevState) {
        if (stream.readBool()) {
            stream.readVector();
        }

        return !stream.getStream().isOverflow();
    }

}
