package me.battledash.kyber.types.pojo.level;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.types.pojo.Asset;
import me.battledash.kyber.types.pojo.render.EnlightenShaderDatabaseBaseAsset;

@Data
@EqualsAndHashCode(callSuper = true)
public class LevelData extends WorldData implements EbxPOJO {

    {
        this.setCreateShaderDatabase(true);
        this.setCreateMaterialGrid(true);
        this.setTimeDeltaType(TimeDeltaType.WORLD);
    }

    private BundleHeapInfo Heap;
    private Asset AISystem;
    private Asset AI2system;
    private float DefaultWorldSizeXZ = 4096.f;
    private float DefaultFOV = 55.f;
    private float InfantryFOVMultiplier = 1.f;
    private float MaxVehicleHeight = 1000.f;
    private boolean HugeBroadPhase = false;
    private EnlightenShaderDatabaseBaseAsset EnlightenShaderDatabase;
    private Asset AntProjectAsset;
    private String AerialHeightmapData;
    private boolean FreeStreamingEnable = true;
    private Object[] AutoLoadedCommonAssetBundles;
    private LevelPreloadInfo PreloadInfo;
    private String[] AutoLoadBundles;

}
