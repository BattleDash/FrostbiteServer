package me.battledash.kyber.network.stream.managers.message;

import lombok.Getter;
import lombok.Setter;
import me.battledash.kyber.engine.messages.Message;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.streams.BitStreamWrite;
import me.battledash.kyber.types.network.NetworkableMessageMetadata;
import me.battledash.kyber.types.network.Initiator;
import me.battledash.kyber.network.ServerConnection;
import me.battledash.kyber.network.stream.managers.ghost.GhostConnection;

@Getter
public abstract class NetworkableMessage extends Message {

    private final boolean hasNetworkedResources;
    private final Initiator initiator;
    private final StreamType messageStream;

    @Setter
    private ServerConnection serverConnection;

    @Setter
    private GhostConnection ghostConnection;

    {
        NetworkableMessageMetadata metadata = this.getClass().getAnnotation(NetworkableMessageMetadata.class);
        this.hasNetworkedResources = metadata.hasNetworkedResources();
        this.initiator = metadata.initiator();
        this.messageStream = metadata.messageStream();
    }

    public boolean generatedReadFrom(BitStreamRead stream) {
        return false;
    }

    public boolean generatedReadFrom(BitStreamRead stream, NetworkableMessageContext ctx) {
        return this.generatedReadFrom(stream);
    }

    public boolean generatedWriteTo(BitStreamWrite stream) {
        return false;
    }

    public boolean generatedWriteTo(BitStreamWrite stream, NetworkableMessageContext ctx) {
        return this.generatedWriteTo(stream);
    }

    public boolean readFrom(BitStreamRead stream, NetworkableMessageContext ctx) {
        ctx.apply(this);

        if (this.hasNetworkedResources) {
            return this.readWithNetworkResources(stream, ctx);
        }
        return this.readNoNetworkResources(stream, ctx);
    }

    public boolean readNoNetworkResources(BitStreamRead stream, NetworkableMessageContext ctx) {
        boolean readSuccess = this.generatedReadFrom(stream, ctx);
        return readSuccess;
    }

    public boolean readWithNetworkResources(BitStreamRead stream, NetworkableMessageContext ctx) {
        long streamStart = stream.getStream().getStreamBitReadPosition();
        long messageSize = stream.readUnsigned(32);

        boolean readSuccess = this.generatedReadFrom(stream, ctx);
        return readSuccess;

        // TODO: 5/5/2022 Handle receiver bit read validation for level streams
    }

    public void writeTo(BitStreamWrite stream, NetworkableMessageContext ctx) {
        int messageSizePos = 0;
        if (this.hasNetworkedResources) {
            messageSizePos = stream.tell();
            stream.writeUnsigned(0, 32);
        }

        this.generatedWriteTo(stream, ctx);

        if (this.hasNetworkedResources) {
            int currentPos = stream.tell();
            stream.seek(messageSizePos);
            stream.writeUnsigned(currentPos - messageSizePos, 32);
            stream.seek(currentPos);
        }
    }

}
