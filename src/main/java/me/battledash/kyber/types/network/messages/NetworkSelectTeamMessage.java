package me.battledash.kyber.types.network.messages;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.battledash.kyber.network.stream.managers.message.NetworkableMessage;
import me.battledash.kyber.network.stream.managers.message.StreamType;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.types.network.Initiator;
import me.battledash.kyber.types.network.NetworkableMessageMetadata;

@Getter
@Setter
@ToString
@NoArgsConstructor
@NetworkableMessageMetadata(hasNetworkedResources = false, initiator = Initiator.CLIENT, messageStream = StreamType.GAME_RELIABLE)
public class NetworkSelectTeamMessage extends NetworkableMessage {

    private int team;

    @Override
    public boolean generatedReadFrom(BitStreamRead stream) {
        this.team = (int) stream.readUnsigned(32);
        return true;
    }

}
