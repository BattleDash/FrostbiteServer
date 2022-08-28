package me.battledash.kyber.types.pojo.material;

import lombok.Data;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
public class BaseMaterialDataPair implements EbxPOJO {

    private BaseMaterialData PhysicsMaterial;
    private BaseMaterialData PhysicsProperty;

}
