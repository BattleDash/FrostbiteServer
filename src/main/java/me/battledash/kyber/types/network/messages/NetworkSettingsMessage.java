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

@Getter
@Setter
@ToString
@NoArgsConstructor
@NetworkableMessageMetadata(hasNetworkedResources = false, initiator = Initiator.SERVER, messageStream = StreamType.GAME_RELIABLE)
public class NetworkSettingsMessage extends NetworkableMessage {

    private boolean disableToggleEntryCamera;
    private boolean disableRegenerateHealth;
    private boolean enableFriendlyFire;
    private boolean allowClientSideDamageArbitration;

    private int difficultyIndex;

    private float bulletDamageModifier;
    private float maxAllowedLatency;
    private float frameHistoryTimeMax;
    private float frameHistoryTime;

    private int moveManagerOutgoingFrequencyDivider;
    private int moveManagerSinglePlayerOutgoingFrequencyDivider;

    @Override
    public boolean generatedReadFrom(BitStreamRead stream) {
        this.disableToggleEntryCamera = stream.readBool();
        this.disableRegenerateHealth = stream.readBool();
        this.enableFriendlyFire = stream.readBool();
        this.allowClientSideDamageArbitration = stream.readBool();

        this.difficultyIndex = (int) stream.readUnsigned(32);

        this.bulletDamageModifier = stream.readFloat();
        this.maxAllowedLatency = stream.readFloat();
        this.frameHistoryTimeMax = stream.readFloat();
        this.frameHistoryTime = stream.readFloat();

        this.moveManagerOutgoingFrequencyDivider = (int) stream.readUnsigned(32);
        this.moveManagerSinglePlayerOutgoingFrequencyDivider = (int) stream.readUnsigned(32);
        return true;
    }

}
