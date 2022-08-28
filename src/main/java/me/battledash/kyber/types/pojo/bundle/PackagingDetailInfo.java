package me.battledash.kyber.types.pojo.bundle;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.types.pojo.Asset;

@Data
@EqualsAndHashCode(callSuper = true)
public class PackagingDetailInfo extends Asset implements EbxPOJO {

    private int MipsToSkip = 0;
    private int LodsToSkip = 0;
    private int LodsToInclude = -1;
    private float AnimationPropagationThreshold = 0.f;
    private boolean PropagateMultiUsedAnimationsFromChildren = true;
    private boolean AutomaticallyPopulateBundleWithAnimations = true;
    private boolean BlockAnimationPropagationToParents = false;
    private boolean IncludeAnimation = false;
    private boolean IncludeMeshes = true;
    private boolean IncludeSounds = false;
    private boolean IncludeTextures = true;
    private boolean IgnoreBundle = false;
    private boolean RemoveOnDemandAnimationsFromLevelBundles = false;

}
