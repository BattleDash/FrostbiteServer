package me.battledash.kyber.types.pojo.level;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.types.pojo.Asset;
import me.battledash.kyber.types.pojo.GameDataContainer;
import me.battledash.kyber.types.pojo.Vec3;
import me.battledash.kyber.types.pojo.render.EnlightenEntityDataWorldPartMap;

@Data
@EqualsAndHashCode(callSuper = true)
public class WorldData extends SubWorldData implements EbxPOJO {

    private float WorldSizeXZ = 0;
    private float WorldSizeY = 1024.f;
    private EnlightenEntityDataWorldPartMap[] EnlightenWorldPartMap;
    private boolean IsProceduralLevel = false;
    private Vec3 ProceduralLevelOffset = new Vec3();
    private Vec3 CellSize = new Vec3(64, 0, 64);
    private boolean AutoLoad = false;

    @SerializedName("BundlingMode")
    private BundlingMode bundlingMode;

    private boolean IsWin32SubLevel = true;
    private boolean IsXb1SubLevel = true;
    private boolean IsPs4SubLevel = true;
    private boolean IsAndroidSubLevel = true;
    private boolean IsIosSubLevel = true;
    private boolean IsOSXSubLevel = true;
    private boolean IsLinuxSubLevel = true;
    private boolean IsNxSubLevel = true;
    private boolean IsPs5SubLevel = true;
    private boolean IsStadiaSubLevel = true;
    private boolean IsXbsxSubLevel = true;
    private boolean IsGen4aSubLevel = true;

    private boolean IsDedicatedServerLevel = true;

    private GameDataContainer[] UpdatePassAssets;
    private GameDataContainer ClientUpdatePassGraph;
    private GameDataContainer ServerUpdatePassGraph;
    private SchematicsBaseAsset SystemEventSchematicsAsset;
    private LevelDescription[] Descriptions;
    private Asset GameConfiguration;
    private String StaticBundleName;
    private Asset[] AdditionalBundles;

    private String ProceduralLevelOwnerAsset;
    private boolean EnableDefaultVe = true;

}
