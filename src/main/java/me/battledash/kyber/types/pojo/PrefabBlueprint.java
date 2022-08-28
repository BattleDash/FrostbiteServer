package me.battledash.kyber.types.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
@EqualsAndHashCode(callSuper = true)
public class PrefabBlueprint extends Blueprint implements EbxPOJO {

    private Vec4 DebugColor;
    private TagAsset[] Tags;
    private TagAsset[] RemovedTags;

}
