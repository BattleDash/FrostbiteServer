package me.battledash.kyber.network.common;

import me.battledash.kyber.network.stream.managers.ghost.GhostConnection;
import me.battledash.kyber.streams.BitStreamRead;

public class Unknown2InputExtent extends EntryInputExtent {

    @Override
    public boolean readFromStream(GhostConnection connection, BitStreamRead stream, EntryInputExtent prevState) {
        if (stream.readBool()) {
            int v5 = 0x40;
            int v6 = 0;
            do
            {
                int v7 = 0x40;
                if ( v5 < 0x40 )
                    v7 = v5;
                long v8 = stream.readUnsigned(((v7 - 0x20) & ((v7 - 0x20) >> 0x1F)) + 0x20);
                if ( v7 > 0x20 )
                    v8 |= stream.readUnsigned(v7 - 0x20) << 0x20;
                v6 = (v6 + 1);
                v5 -= v7;
            }
            while ( v5 > 0 );
            stream.readFloat();
            stream.readFloat();
            stream.readFloat();
            stream.readFloat();
            stream.readFloat();
            stream.readFloat();
        }

        return !stream.getStream().isOverflow();
    }

}
