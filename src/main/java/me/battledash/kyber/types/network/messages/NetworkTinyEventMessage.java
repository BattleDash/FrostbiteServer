package me.battledash.kyber.types.network.messages;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.streams.BitStreamWrite;
import me.battledash.kyber.types.network.Initiator;
import me.battledash.kyber.types.network.NetworkableMessageMetadata;
import me.battledash.kyber.network.TinyEvent;
import me.battledash.kyber.network.stream.managers.message.NetworkableMessage;
import me.battledash.kyber.network.stream.managers.message.StreamType;

@Setter
@Getter
@ToString
@NoArgsConstructor
@NetworkableMessageMetadata(hasNetworkedResources = false, initiator = Initiator.ANY, messageStream = StreamType.GAME_RELIABLE)
public class NetworkTinyEventMessage extends NetworkableMessage {

    private static final TinyEvent[] TINY_EVENTS = TinyEvent.values();

    private TinyEvent tinyEvent;

    @Override
    public boolean generatedReadFrom(BitStreamRead stream) {
        int event = (int) stream.readUnsignedLimit(1, NetworkTinyEventMessage.TINY_EVENTS.length);
        if (event <= NetworkTinyEventMessage.TINY_EVENTS.length) {
            this.tinyEvent = NetworkTinyEventMessage.TINY_EVENTS[event - 1];
            return true;
        }
        return false;
    }

    @Override
    public boolean generatedWriteTo(BitStreamWrite stream) {
        stream.writeUnsignedLimit(this.tinyEvent.ordinal() + 1, 1, NetworkTinyEventMessage.TINY_EVENTS.length);
        return true;
    }

}
