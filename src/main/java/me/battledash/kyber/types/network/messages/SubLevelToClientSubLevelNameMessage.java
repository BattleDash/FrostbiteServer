package me.battledash.kyber.types.network.messages;

import lombok.AllArgsConstructor;
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

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@NetworkableMessageMetadata(hasNetworkedResources = false, initiator = Initiator.SERVER, messageStream = StreamType.GAME_RELIABLE)
public class SubLevelToClientSubLevelNameMessage extends NetworkableMessage {

    private long levelSequenceNumber;
    private List<BundleNameAndIndex> bundles = new ArrayList<>();

    @Override
    public boolean generatedReadFrom(BitStreamRead stream) {
        this.levelSequenceNumber = stream.readUnsigned(32);
        int size = (int) stream.readUnsigned(12);
        if (size > 2048) {
            return false;
        }
        this.bundles.clear();
        for (int i = 0; i < size; i++) {
            BundleNameAndIndex bundle = new BundleNameAndIndex();
            bundle.setIndex((int) stream.readUnsigned(16));
            bundle.setName(stream.readString());
            this.bundles.add(bundle);
        }
        return true;
    }

    @Override
    public boolean generatedWriteTo(BitStreamWrite stream) {
        stream.writeUnsigned(this.levelSequenceNumber, 32);
        stream.writeUnsigned(this.bundles.size(), 12);
        for (BundleNameAndIndex bundle : this.bundles) {
            stream.writeUnsigned(bundle.getIndex(), 16);
            stream.writeString(bundle.getName());
        }
        return true;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BundleNameAndIndex {
        private int index;
        private String name;
    }

}
