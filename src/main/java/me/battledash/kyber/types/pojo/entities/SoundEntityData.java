package me.battledash.kyber.types.pojo.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.types.pojo.Asset;
import me.battledash.kyber.types.pojo.LinearTransform;

@Data
@EqualsAndHashCode(callSuper = true)
public class SoundEntityData extends EntityData implements EbxPOJO {

    private float MasterAmplitude;
    private LinearTransform Transform;
    private Asset Sound;
    private boolean PlayOnCreation;
    private boolean EnableOnCreation;
    private boolean UseParentTransform;
    private boolean ForgetOnDestroy;

}
