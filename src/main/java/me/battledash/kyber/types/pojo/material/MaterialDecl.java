package me.battledash.kyber.types.pojo.material;

import lombok.Data;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
public class MaterialDecl implements EbxPOJO {

    private BaseMaterialDataPair BasePair;

    private long Packed;

}
