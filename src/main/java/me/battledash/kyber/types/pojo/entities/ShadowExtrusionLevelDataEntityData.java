package me.battledash.kyber.types.pojo.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.types.RenderingOverrides;
import me.battledash.kyber.types.pojo.Vec3;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShadowExtrusionLevelDataEntityData extends EntityData implements EbxPOJO {

    private Vec3[] ExtrusionDirections;

}
