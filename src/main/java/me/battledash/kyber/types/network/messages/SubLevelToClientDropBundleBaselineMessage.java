package me.battledash.kyber.types.network.messages;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@NetworkableMessageMetadata(hasNetworkedResources = false, initiator = Initiator.SERVER, messageStream = StreamType.GAME_RELIABLE)
public class SubLevelToClientDropBundleBaselineMessage extends NetworkableMessage {

    private long levelSequenceNumber;

    @Override
    public boolean generatedReadFrom(BitStreamRead stream) {
        this.levelSequenceNumber = stream.readUnsigned(32);
        return true;
    }

    @Override
    public boolean generatedWriteTo(BitStreamWrite stream) {
        stream.writeUnsigned(this.levelSequenceNumber, 32);
        return true;
    }

}
