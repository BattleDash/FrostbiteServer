package me.battledash.kyber.types.pojo.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.types.Realm;
import me.battledash.kyber.types.pojo.Asset;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
@EqualsAndHashCode(callSuper = true)
public class PathfindingSystemEntityData extends EntityData implements EbxPOJO {

    private long[] PathfindingTypesOnLevel;
    private Realm Realm;
    private Asset Asset;

}
