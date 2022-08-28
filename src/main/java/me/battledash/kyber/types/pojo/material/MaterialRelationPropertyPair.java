package me.battledash.kyber.types.pojo.material;

import lombok.Data;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
public class MaterialRelationPropertyPair implements EbxPOJO {

    private PhysicsMaterialRelationPropertyData[] PhysicsMaterialProperties;
    private PhysicsPropertyRelationPropertyData[] PhysicsPropertyProperties;

}
