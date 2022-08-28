package me.battledash.kyber.types.pojo.bundle;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.types.pojo.Asset;
import me.battledash.kyber.types.pojo.Vec3;

@Data
@EqualsAndHashCode(callSuper = true)
public class PackagingRule extends Asset implements EbxPOJO {

    private Vec3 DebugColor;
    private boolean CanTargetSelf;

}
