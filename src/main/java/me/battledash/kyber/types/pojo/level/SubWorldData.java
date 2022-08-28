package me.battledash.kyber.types.pojo.level;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.types.pojo.Asset;
import me.battledash.kyber.types.pojo.SpatialPrefabBlueprint;
import me.battledash.kyber.types.pojo.bundle.AutoAssetCollectorAsset;
import me.battledash.kyber.types.pojo.bundle.CellDetailSelectionRule;
import me.battledash.kyber.types.pojo.material.MaterialGridData;

@Data
@EqualsAndHashCode(callSuper = true)
public class SubWorldData extends SpatialPrefabBlueprint implements EbxPOJO {

    {
        this.setCreateGroupEntity(true);
        this.setGroupSupportsKeyframing(true);
    }

    private boolean IsBundleRoot = true;
    private String Category;
    private MaterialGridData RuntimeMaterialGrid;
    private boolean AutoLoad = false;
    private boolean SeparateLateMountedSuperBundle = false;

    @SerializedName("BundlingMode")
    private BundlingMode bundlingMode = BundlingMode.DEFAULT;

    private boolean CreateShaderDatabase = false;
    private boolean BlockAssetCollectionByParent;
    private Asset ExternalShaderConditionalFilter;
    private boolean CreateMaterialGrid = false;
    private SubWorldInclusionSettings InclusionSettings;
    private BundleHeapInfo HeapInfo;
    private Asset TerrainResolutionLayout;
    private CellDetailSelectionRule StandalonePackagingRule;

    private boolean IsWin32SubLevel = true;
    private boolean IsXb1SubLevel = true;
    private boolean IsPs4SubLevel = true;
    private boolean IsAndroidSubLevel = true;
    private boolean IsIosSubLevel = true;
    private boolean IsOsxSubLevel = true;
    private boolean IsLinuxSubLevel = true;
    private boolean IsNxSubLevel = true;
    private boolean IsPs5SubLevel = true;
    private boolean IsStadiaSubLevel = true;
    private boolean IsXbsxSubLevel = true;

    private BlueprintPersistenceSetting PersistenceSetting = BlueprintPersistenceSetting.INHERIT_FROM_PARENT;
    private boolean PromoteWeaponsAndUnlocksFromSubLevels = false;
    private Asset[] ExplicitlyBundledAssets;
    private boolean ExcludeFromGameView = false;
    private AutoAssetCollectorAsset AutoAssetCollector;
    private boolean UsePeerFiltering = false;
    private boolean SuppressLightmapGenEntityGeneration = false;
    private SubWorldDataComponent[] Components;

    public enum BundlingMode {
        DEFAULT,
        STATIC,
        DYNAMIC,
        CHUNK_HINT_LIST,
        SHADER_ONLY
    }

    public enum BlueprintPersistenceSetting {
        INHERIT_FROM_PARENT,
        SAVED_WHEN_IN_MEMORY,
        ALWAYS_SAVED,
        NEVER_SAVED
    }

    public static class SubWorldDataComponent implements EbxPOJO {
    }

}
