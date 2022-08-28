package me.battledash.kyber.types.pojo.level;

import lombok.Data;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
public class SubLevelPreloadInfo implements EbxPOJO {

    private String SubLevelBundlePath;
    private String[] PreloadedBlueprintBundles;

}
