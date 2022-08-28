package me.battledash.kyber.types.network.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.types.network.Initiator;
import me.battledash.kyber.types.network.NetworkableMessageMetadata;
import me.battledash.kyber.network.stream.managers.message.NetworkableMessage;
import me.battledash.kyber.network.stream.managers.message.StreamType;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@NetworkableMessageMetadata(hasNetworkedResources = false, initiator = Initiator.SERVER, messageStream = StreamType.GAME_RELIABLE)
public class WSNetworkServerInfoMessage extends NetworkableMessage {

    private int unknown1;
    private int unknown2;

    @Override
    public boolean generatedReadFrom(BitStreamRead stream) {
        this.unknown1 = (int) stream.readUnsigned(32);
        this.unknown2 = (int) stream.readUnsigned(32);
        return true;
    }

}
