package me.battledash.kyber.types.pojo.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.types.Realm;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrEntityData extends EntityData implements EbxPOJO {

    private Realm Realm;
    private long InputCount;

}
