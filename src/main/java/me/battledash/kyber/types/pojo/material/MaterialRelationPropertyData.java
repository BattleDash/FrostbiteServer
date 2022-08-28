package me.battledash.kyber.types.pojo.material;

import lombok.Data;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
public class MaterialRelationPropertyData implements EbxPOJO {

    private BaseMaterialData SourceMaterial;
    private long SourceMaterialIndex = 0xFFFFFFFF;

}
