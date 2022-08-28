package me.battledash.kyber.types.pojo.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.types.RenderingOverrides;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
@EqualsAndHashCode(callSuper = true)
public class TerrainEntityData extends GamePhysicsEntityData implements EbxPOJO {

    private TerrainData TerrainAsset;

    public static class TerrainBaseAsset implements EbxPOJO {
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class TerrainData extends TerrainBaseAsset implements EbxPOJO {
        private boolean DynamicMaskEnable = false;
    }

}
