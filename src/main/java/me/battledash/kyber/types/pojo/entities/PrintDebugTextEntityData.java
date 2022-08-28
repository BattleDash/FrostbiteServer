package me.battledash.kyber.types.pojo.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.types.Realm;
import me.battledash.kyber.types.pojo.Vec2;
import me.battledash.kyber.types.pojo.Vec3;

@Data
@EqualsAndHashCode(callSuper = true)
public class PrintDebugTextEntityData extends EntityData implements EbxPOJO {

    private Vec2 ScreenPosition;
    private Realm Realm;
    private String Text;
    private float TimeToShow;
    private Vec3 TextColor;
    private Vec3 WorldPosition;
    private float TextScale;
    private boolean UseWorldPosition;
    private boolean VisibleAtStart;
    private boolean Enabled;

}
