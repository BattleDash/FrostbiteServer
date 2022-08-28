package me.battledash.kyber.types.pojo.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.types.Realm;
import me.battledash.kyber.types.pojo.Asset;
import me.battledash.kyber.types.pojo.entities.structs.FadeCurveType;

@Data
@EqualsAndHashCode(callSuper = true)
public class DiceSoundAreaEntityData extends EntityData implements EbxPOJO {

    private float PerimeterSize;
    private Asset Sound;
    private Asset BigWorld;
    private float RelevanceMultiplier;
    private float MinRelevanceBudget;
    private FadeCurveType RelevanceFalloff;
    private float Priority;
    private boolean EnableOnCreation;
    private boolean UseLegacyBehavior;
    private boolean FaceListener;
    private boolean IgnoreVerticlePerimeter;

}
