package me.battledash.kyber.types.network.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.streams.BitStreamWrite;
import me.battledash.kyber.types.network.NetworkableMessageMetadata;
import me.battledash.kyber.types.network.Initiator;
import me.battledash.kyber.network.stream.managers.message.NetworkableMessage;
import me.battledash.kyber.network.stream.managers.message.StreamType;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@NetworkableMessageMetadata(hasNetworkedResources = false, initiator = Initiator.CLIENT, messageStream = StreamType.GAME_RELIABLE)
public class NetworkCreatePlayerMessage extends NetworkableMessage {

    private String name;
    private boolean isSpectator;

    @Override
    public boolean generatedReadFrom(BitStreamRead stream) {
        this.name = stream.readString();
        this.isSpectator = stream.readBool();
        return true;
    }

    @Override
    public boolean generatedWriteTo(BitStreamWrite stream) {
        stream.writeString(this.name);
        stream.writeBool(this.isSpectator);
        return true;
    }

}
