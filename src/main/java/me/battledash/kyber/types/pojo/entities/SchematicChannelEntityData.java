package me.battledash.kyber.types.pojo.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.types.Realm;
import me.battledash.kyber.types.pojo.Asset;

@Data
@EqualsAndHashCode(callSuper = true)
public class SchematicChannelEntityData extends EntityData implements EbxPOJO {

    private Realm Realm;
    private Asset Channel;
    private int[] InputProperties;
    private int[] InputRefProperties;
    private int[] OutputProperties;
    private int[] OutputRefProperties;

}
