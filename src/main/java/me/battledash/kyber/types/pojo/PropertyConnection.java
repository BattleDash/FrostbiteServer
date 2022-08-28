package me.battledash.kyber.types.pojo;

import lombok.Data;
import me.battledash.kyber.fs.AssetManager;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.fs.ebx.NotifyDeserialized;
import me.battledash.kyber.server.ServerGameContext;

import java.util.Map;

@Data
public class PropertyConnection implements EbxPOJO, NotifyDeserialized {

    private String SourceField;
    private String TargetField;

    private Object Source;
    private Object Target;

    private int SourceFieldId;
    private int TargetFieldId;

    @Override
    public void onDeserialized() {
        AssetManager assetManager = ServerGameContext.context().getServer().getAssetManager();
        this.SourceField = assetManager.getHashedString(this.SourceFieldId);
        this.TargetField = assetManager.getHashedString(this.TargetFieldId);
    }

}
