package me.battledash.kyber.types.pojo.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.types.pojo.LinearTransform;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpatialEntityData extends EntityData implements EbxPOJO {

    private LinearTransform Transform;

}
