package me.battledash.kyber.types.pojo.material;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.types.pojo.Asset;

@Data
@EqualsAndHashCode(callSuper = true)
public class MaterialGridData extends Asset implements EbxPOJO {

    private MaterialDecl DefaultMaterial;
    private MaterialDecl[] MaterialPairs;
    private long[] MaterialIndexMap;
    private long DefaultMaterialIndex;

    private MaterialRelationPropertyPair[] MaterialProperties;
    private MaterialInteractionGridRow[] InteractionGrid;

}
