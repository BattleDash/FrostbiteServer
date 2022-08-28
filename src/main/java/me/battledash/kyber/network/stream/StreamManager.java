package me.battledash.kyber.network.stream;

import lombok.Getter;
import lombok.Setter;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.streams.BitStreamWrite;

public abstract class StreamManager {

    @Setter
    @Getter
    private boolean debugBroadcastProcessing = false;

    public abstract String getName();

    public void preTransmitPacket() {
    }

    public void postTransmitPacket() {
    }

    public TransmitResult transmitPacket(BitStreamWrite stream) {
        return TransmitResult.NOTHING_TO_SEND;
    }

    public boolean processReceivedPacket(BitStreamRead stream) {
        return true;
    }

}
