package me.battledash.kyber.types.network.messages;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.types.network.Initiator;
import me.battledash.kyber.types.network.NetworkableMessageMetadata;
import me.battledash.kyber.network.stream.managers.message.NetworkableMessage;
import me.battledash.kyber.network.stream.managers.message.StreamType;

import java.util.UUID;

@Setter
@Getter
@ToString
@NoArgsConstructor
@NetworkableMessageMetadata(hasNetworkedResources = false, initiator = Initiator.CLIENT, messageStream = StreamType.GAME_RELIABLE)
public class NetworkLevelLoadedAckMessage extends NetworkableMessage {

    private String name;
    private UUID checksum;

    @Override
    public boolean generatedReadFrom(BitStreamRead stream) {
        this.name = stream.readString();
        int guid1 = (int) stream.readUnsigned(32);
        int guid2 = (int) stream.readUnsigned(16);
        int guid3 = (int) stream.readUnsigned(16);

        byte[] guidBytes = new byte[8];
        guidBytes[0] = (byte) (guid1 >> 24);
        guidBytes[1] = (byte) (guid1 >> 16);
        guidBytes[2] = (byte) (guid1 >> 8);
        guidBytes[3] = (byte) (guid1);
        guidBytes[4] = (byte) (guid2 >> 8);
        guidBytes[5] = (byte) (guid2);
        guidBytes[6] = (byte) (guid3 >> 8);
        guidBytes[7] = (byte) (guid3);

        this.checksum = UUID.nameUUIDFromBytes(guidBytes);
        return true;
    }

}
