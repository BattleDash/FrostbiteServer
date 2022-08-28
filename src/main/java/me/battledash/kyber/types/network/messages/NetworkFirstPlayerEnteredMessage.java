package me.battledash.kyber.types.network.messages;

import lombok.ToString;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.streams.BitStreamWrite;
import me.battledash.kyber.types.network.Initiator;
import me.battledash.kyber.types.network.NetworkableMessageMetadata;
import me.battledash.kyber.network.stream.managers.message.NetworkableMessage;
import me.battledash.kyber.network.stream.managers.message.StreamType;

@ToString
@NetworkableMessageMetadata(hasNetworkedResources = false, initiator = Initiator.CLIENT, messageStream = StreamType.GAME_RELIABLE)
public class NetworkFirstPlayerEnteredMessage extends NetworkableMessage {

    @Override
    public boolean generatedReadFrom(BitStreamRead stream) {
        return true;
    }

    @Override
    public boolean generatedWriteTo(BitStreamWrite stream) {
        return true;
    }

}
