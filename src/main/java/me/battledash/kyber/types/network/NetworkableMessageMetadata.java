package me.battledash.kyber.types.network;

import me.battledash.kyber.network.stream.managers.message.StreamType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface NetworkableMessageMetadata {
    boolean hasNetworkedResources();
    Initiator initiator();
    StreamType messageStream();
}
