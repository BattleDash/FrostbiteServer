package me.battledash.kyber.types.network.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.types.network.Initiator;
import me.battledash.kyber.types.network.NetworkableMessageMetadata;
import me.battledash.kyber.network.stream.managers.ghost.Ghost;
import me.battledash.kyber.network.stream.managers.message.NetworkableMessage;
import me.battledash.kyber.network.stream.managers.message.NetworkableMessageContext;
import me.battledash.kyber.network.stream.managers.message.StreamType;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@NetworkableMessageMetadata(hasNetworkedResources = false, initiator = Initiator.SERVER, messageStream = StreamType.GAME_RELIABLE)
public class NetworkOnPlayerSpawnedMessage extends NetworkableMessage {

    private Ghost player;

    @Override
    public boolean generatedReadFrom(BitStreamRead stream, NetworkableMessageContext ctx) {
        this.player = ctx.getGhostConnection().readGhostId(stream);
        return true;
    }

}
