package me.battledash.kyber.types.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
@EqualsAndHashCode(callSuper = true)
public class ComponentData extends GameObjectData implements EbxPOJO {

    boolean HasTransform = false;
    LinearTransform Transform;
    GameObjectData[] Components;
    long ClientIndex = 0xff;
    long ServerIndex = 0xff;
    boolean IsExposedToSubLevels;
    boolean Excluded = false;
    static boolean AllowClientRealmForInputPropertyTransform = false;

}
