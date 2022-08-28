package me.battledash.kyber.types.network.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.battledash.kyber.streams.BitStreamRead;
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
public class WSNetworkKitLimitationLockKitsResultMessage extends NetworkableMessage {

    private int unknown1;
    private int unknown2;
    private int unknown3;
    private boolean unknown4;
    private long unknown5;

    @Override
    public boolean generatedReadFrom(BitStreamRead stream) {
        this.unknown1 = (int) stream.readUnsigned(32);
        this.unknown1 = (int) stream.readUnsigned(32);
        this.unknown1 = (int) stream.readUnsigned(32);
        this.unknown4 = stream.readBool();
        this.unknown5 = stream.readUnsignedLimit(0, 255);
        return this.unknown5 >= 0 && (this.unknown5 <= 7 || (this.unknown5 - 253) <= 2);
    }

}
