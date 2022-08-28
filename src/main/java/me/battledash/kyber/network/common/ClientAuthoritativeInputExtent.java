package me.battledash.kyber.network.common;

import me.battledash.kyber.network.stream.managers.ghost.GhostConnection;
import me.battledash.kyber.streams.BitStreamRead;

public class ClientAuthoritativeInputExtent extends EntryInputExtent {

    @Override
    public boolean readFromStream(GhostConnection connection, BitStreamRead stream, EntryInputExtent prevState) {
        if (stream.readBool()) {
            stream.readFloat();
        }
        stream.readBool();

        stream.readBool();
        stream.readBool();
        stream.readBool();

        if (stream.readBool()) {
            if (stream.readBool()) {
                if (stream.readBool()) {
                    stream.readBool();
                    stream.readBool();
                } else {
                    stream.readVector();
                }
            } else {
                stream.readBool();
            }
        }

        stream.readBool();
        stream.readBool();
        stream.readBool();

        boolean readAuthoritativeAiming = stream.readBool();
        if (readAuthoritativeAiming && stream.readBool()) {
            stream.readSignedFloatScale(32L, (float) (2.0f * Math.PI));
        }

        if (readAuthoritativeAiming && stream.readBool()) {
            stream.readSignedFloatScale(32, (float) Math.PI);
        }

        if (readAuthoritativeAiming && stream.readBool()) {
            stream.readSignedFloatScale(32L, (float) (2.0f * Math.PI));
            stream.readSignedFloatScale(32, (float) Math.PI);
        }

        if (readAuthoritativeAiming && stream.readBool()) {
            stream.readUnsignedUnitFloat(32);
        }

        if (stream.readBool()) {
            stream.readSignedFloatScale(5, 1.0f);
            stream.readUnsignedFloatScale(5, 2.0f);
            stream.readSignedFloatScale(5, 1.0f);
        }

        boolean readAuthoritativeCamera = stream.readBool();

        if (readAuthoritativeCamera && stream.readBool()) {
            stream.readUnsignedFloatScale(32, (float) (2.0f * Math.PI));
        }

        if (readAuthoritativeCamera && stream.readBool()) {
            stream.readUnsignedFloatScale(32L, (float) Math.PI);
        }

        return !stream.getStream().isOverflow();
    }

}
