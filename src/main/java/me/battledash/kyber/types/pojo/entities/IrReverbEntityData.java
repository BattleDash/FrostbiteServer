package me.battledash.kyber.types.pojo.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.types.Realm;
import me.battledash.kyber.types.pojo.Asset;
import me.battledash.kyber.types.pojo.entities.structs.FadeCurveType;

@Data
@EqualsAndHashCode(callSuper = true)
public class IrReverbEntityData extends EntityData implements EbxPOJO {

    private float Gain;
    private Asset ImpulseResponse;
    private float Volume;
    private FadeCurveType FadeCurve;
    private boolean IsDominant;

}
