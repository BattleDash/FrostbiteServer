package me.battledash.kyber.types.pojo.material;

import lombok.Data;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
public class MaterialCategory implements EbxPOJO {

    private String Name;
    private boolean IsPhysicsMaterialCategory;
    private boolean IsPhysicsPropertyCategory;

}
