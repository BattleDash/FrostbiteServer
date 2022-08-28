package me.battledash.kyber.types.pojo.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.types.pojo.Asset;

@Data
@EqualsAndHashCode(callSuper = true)
public class DynamicEnlightenEntityData extends EnlightenEntityData implements EbxPOJO {

    private int Priority;
    private Asset EnlightenData;
    private int DatabaseVersion;
    private int ObjectLayers;
    private boolean Enable;

}
