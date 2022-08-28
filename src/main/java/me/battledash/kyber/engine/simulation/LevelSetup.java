package me.battledash.kyber.engine.simulation;

import lombok.Data;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.streams.BitStreamWrite;

import java.util.ArrayList;
import java.util.List;

@Data
public class LevelSetup {

    private String name;
    private List<LevelSetupOption> inclusionOptions = new ArrayList<>();
    private int difficultyIndex;
    private String startPoint = "";
    private boolean isSaveGame;
    private boolean hasPersistentSave;
    private boolean forceReloadResources;
    private String initialDSubLevel = "";
    private String initialStartPoint = "";

    public void setInclusionOption(String criterion, String value) {
        this.inclusionOptions.add(new LevelSetupOption(criterion, value));
    }

    public String getInclusionOption(String criterion) {
        for (LevelSetupOption option : this.inclusionOptions) {
            if (option.getCriterion().equals(criterion)) {
                return option.getValue();
            }
        }
        return null;
    }

    public void readFrom(BitStreamRead stream) {
        this.name = stream.readString();
        int inclusionOptionsCount = (int) stream.readUnsigned(12);
        for (int i = 0; i < inclusionOptionsCount; i++) {
            this.inclusionOptions.add(new LevelSetupOption(stream.readString(), stream.readString()));
        }
        this.difficultyIndex = (int) stream.readUnsigned(32);
        this.startPoint = stream.readString();
        this.isSaveGame = stream.readBool();
        this.hasPersistentSave = stream.readBool();
        this.forceReloadResources = stream.readBool();
        this.initialDSubLevel = stream.readString();
        this.initialStartPoint = stream.readString();
    }

    public void writeTo(BitStreamWrite stream) {
        stream.writeString(this.name);
        stream.writeUnsigned(this.inclusionOptions.size(), 12);
        for (LevelSetupOption option : this.inclusionOptions) {
            stream.writeString(option.getCriterion());
            stream.writeString(option.getValue());
        }
        stream.writeUnsigned(this.difficultyIndex, 32);
        stream.writeString(this.startPoint);
        stream.writeBool(this.isSaveGame);
        stream.writeBool(this.hasPersistentSave);
        stream.writeBool(this.forceReloadResources);
        stream.writeString(this.initialDSubLevel);
        stream.writeString(this.initialStartPoint);
    }

    @Data
    public static class LevelSetupOption {
        private final String criterion;
        private final String value;
    }

}
