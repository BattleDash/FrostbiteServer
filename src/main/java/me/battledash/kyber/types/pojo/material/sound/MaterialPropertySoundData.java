package me.battledash.kyber.types.pojo.material.sound;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.types.pojo.material.PhysicsMaterialRelationPropertyData;

@Data
@EqualsAndHashCode(callSuper = true)
public class MaterialPropertySoundData extends PhysicsMaterialRelationPropertyData implements EbxPOJO {

    private float ScrapeLength = 1.f;
    private float Softness = 0.f;
    private float MaterialSoundId = -1.f;

}
