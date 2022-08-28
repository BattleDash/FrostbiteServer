package me.battledash.kyber.types.network.messages;

import lombok.Getter;
import lombok.ToString;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.types.network.NetworkableMessageMetadata;
import me.battledash.kyber.types.network.Initiator;
import me.battledash.kyber.network.stream.managers.message.NetworkableMessage;
import me.battledash.kyber.network.stream.managers.message.StreamType;

@Getter
@ToString
@NetworkableMessageMetadata(hasNetworkedResources = false, initiator = Initiator.CLIENT, messageStream = StreamType.GAME_RELIABLE)
public class OnlineNetworkPlayerGameGroupChangedMessage extends NetworkableMessage {

    private long unknown1;
    private long unknown2;

    @Override
    public boolean generatedReadFrom(BitStreamRead stream) {
        this.unknown1 = stream.readUnsigned64(64);
        this.unknown2 = stream.readUnsigned64(64);
        return true;
    }

}
