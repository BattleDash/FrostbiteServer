package me.battledash.kyber.types.network.messages;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.battledash.kyber.engine.simulation.LevelSetup;
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
public class NetworkLoadLevelMessage extends NetworkableMessage {

    private LoadLevelInfo info = new LoadLevelInfo();

    @Override
    public boolean generatedReadFrom(BitStreamRead stream) {
        LoadLevelInfo info = new LoadLevelInfo();
        LevelSetup levelSetup = new LevelSetup();
        levelSetup.readFrom(stream);
        info.setSetup(levelSetup);
        int preloadCount = (int) stream.readUnsigned(12);
        if (preloadCount >= 2048) {
            return false;
        }
        BlueprintBundlePreloadInfo[] preloads = new BlueprintBundlePreloadInfo[preloadCount];
        for (int i = 0; i < preloads.length; i++) {
            BlueprintBundlePreloadInfo preload = new BlueprintBundlePreloadInfo();
            preload.setName(stream.readString());
            preload.setCompartment((int) stream.readUnsigned(32));
            preload.setParentCompartment((int) stream.readUnsigned(32));
        }
        info.setBlueprintBundlePreloads(preloads);
        info.setLevelSequenceNumber((int) stream.readUnsigned(32));
        this.info = info;
        return true;
    }

    @Override
    public boolean generatedWriteTo(BitStreamWrite stream) {
        this.info.getSetup().writeTo(stream);
        BlueprintBundlePreloadInfo[] preloads = this.info.getBlueprintBundlePreloads();
        stream.writeUnsigned(preloads.length, 12);
        for (BlueprintBundlePreloadInfo preload : preloads) {
            stream.writeString(preload.getName());
            stream.writeUnsigned(preload.getCompartment(), 32);
            stream.writeUnsigned(preload.getParentCompartment(), 32);
        }
        stream.writeUnsigned(this.info.getLevelSequenceNumber(), 32);
        return true;
    }

    @Data
    public static class LoadLevelInfo {
        private LevelSetup setup = new LevelSetup();
        private BlueprintBundlePreloadInfo[] blueprintBundlePreloads = new BlueprintBundlePreloadInfo[0];
        private long levelSequenceNumber;
    }

    @Data
    public static class BlueprintBundlePreloadInfo {
        private String name;
        private int compartment;
        private int parentCompartment;
    }

}
