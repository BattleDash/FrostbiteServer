package me.battledash.kyber.types.pojo.material;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.types.pojo.Vec3;

@Data
public class BaseMaterialData implements EbxPOJO {

    private String Name;

    @SerializedName("MaterialCategory")
    private MaterialCategory materialCategory;

    private Vec3 DebugColor = new Vec3(1.f, 0.f, 1.f);
    private boolean Penetrable = false;
    private boolean ClientDestructible = false;
    private boolean Bashable = false;
    private boolean SeeThrough = false;
    private boolean NoCollisionResponse = false;
    private boolean NoCollisionResponseCombined = false;
    private boolean Attachable = false;
    private boolean Deprecated = false;

    private BaseMaterialData Parent;

}
