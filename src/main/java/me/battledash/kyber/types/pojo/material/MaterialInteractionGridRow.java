package me.battledash.kyber.types.pojo.material;

import lombok.Data;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
public class MaterialInteractionGridRow implements EbxPOJO {

    private MaterialRelationPropertyPair[] Items;

}
