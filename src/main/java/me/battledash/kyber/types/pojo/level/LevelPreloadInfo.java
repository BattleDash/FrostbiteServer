package me.battledash.kyber.types.pojo.level;

import lombok.Data;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
public class LevelPreloadInfo implements EbxPOJO {

    private String[] PreloadedBlueprintBundles;
    private SubLevelPreloadInfo[] SubLevelPreloadInfoMap;

}
