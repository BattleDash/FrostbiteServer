package me.battledash.kyber.types.network.messages.internal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.battledash.kyber.engine.entity.EntityBus;
import me.battledash.kyber.engine.entity.network.GhostEntityHelper;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.streams.BitStreamWrite;
import me.battledash.kyber.types.network.Initiator;
import me.battledash.kyber.types.network.NetworkableMessageMetadata;
import me.battledash.kyber.network.stream.managers.message.NetworkableMessage;
import me.battledash.kyber.network.stream.managers.message.NetworkableMessageContext;
import me.battledash.kyber.network.stream.managers.message.StreamType;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
@NoArgsConstructor
@NetworkableMessageMetadata(hasNetworkedResources = false, initiator = Initiator.SERVER, messageStream = StreamType.GAME_RELIABLE)
public class NetworkLogicFirePlayerEventMessage extends NetworkableMessage {

    private List<Integer> eventIndex = new ArrayList<>();
    private int playerId = -1;
    private EntityBus bus;

    @Override
    public boolean generatedWriteTo(BitStreamWrite stream) {
        return super.generatedWriteTo(stream);
    }

    @Override
    public boolean generatedReadFrom(BitStreamRead stream, NetworkableMessageContext ctx) {
        // TODO: 5/9/2022 Use core.engine.entity.network.GhostEntityHelper
        int eventCount = (int) stream.readUnsigned(12);
        if (eventCount > 2048) {
            return false;
        }
        for (int i = 0; i < eventCount; i++) {
            eventIndex.add((int) stream.readUnsigned(32));
        }
        this.playerId = (int) stream.readUnsigned(32);
        this.bus = GhostEntityHelper.readEntityBus(stream, ctx.getGhostConnection(), true);
        return true;
    }

}
