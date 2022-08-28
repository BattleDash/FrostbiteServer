package me.battledash.kyber.types.pojo.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.types.Realm;
import me.battledash.kyber.types.pojo.Asset;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShadowExtrusionDataEntityData extends EntityData implements EbxPOJO {

    private Asset ExtrusionData;
    private Realm Realm;

}
