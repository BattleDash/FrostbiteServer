package me.battledash.kyber.types.network.messages;

import lombok.Data;
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
public class StatImmutableMessage extends NetworkableMessage {

    private ImmutableStat[] stats = new ImmutableStat[0];

    @Override
    public boolean generatedReadFrom(BitStreamRead stream) {
        int size = (int) stream.readUnsigned(12);
        if (size > 2048) {
            return false;
        }
        this.stats = new ImmutableStat[size];
        for (int i = 0; i < size; i++) {
            ImmutableStat stat = new ImmutableStat();
            stat.setName(stream.readString());
            stat.setCompartment((int) stream.readUnsigned(32));
            this.stats[i] = stat;
        }
        return true;
    }

    @Data
    public static class ImmutableStat {
        private String name;
        private int compartment;
    }

}
