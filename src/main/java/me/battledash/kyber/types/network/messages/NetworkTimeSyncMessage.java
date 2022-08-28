package me.battledash.kyber.types.network.messages;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.streams.BitStreamWrite;
import me.battledash.kyber.types.network.Initiator;
import me.battledash.kyber.types.network.NetworkableMessageMetadata;
import me.battledash.kyber.network.stream.managers.message.NetworkableMessage;
import me.battledash.kyber.network.stream.managers.message.StreamType;

@Setter
@Getter
@ToString
@NoArgsConstructor
@NetworkableMessageMetadata(hasNetworkedResources = false, initiator = Initiator.SERVER, messageStream = StreamType.GAME_RELIABLE)
public class NetworkTimeSyncMessage extends NetworkableMessage {

    private boolean triggerTimeSync;
    private int ticks;
    private double timeCastToDouble;

    @Override
    public boolean generatedReadFrom(BitStreamRead stream) {
        this.triggerTimeSync = stream.readBool();
        this.ticks = (int) stream.readUnsigned(32);
        this.timeCastToDouble = stream.readUnsigned64(64);
        return true;
    }

    @Override
    public boolean generatedWriteTo(BitStreamWrite stream) {
        stream.writeBool(this.triggerTimeSync);
        stream.writeUnsigned(this.ticks, 32);
        stream.writeUnsigned64((long) this.timeCastToDouble, 64);
        return true;
    }

}
