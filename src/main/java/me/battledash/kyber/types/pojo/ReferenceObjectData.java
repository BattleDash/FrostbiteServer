package me.battledash.kyber.types.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.types.RenderingOverrides;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReferenceObjectData extends GameObjectData implements EbxPOJO {

    private LinearTransform BlueprintTransform;
    private Blueprint Blueprint;
    private TemplateAsset Template;
    private Blueprint InterfaceBlueprint;
    private ObjectVariation ObjectVariation;
    private long ObjectSubVariation;
    private long RefreshSubVariationChoiceList;
    private RenderingOverrides.RadiosityTypeOverride RadiosityTypeOverride = RenderingOverrides.RadiosityTypeOverride.NONE;
    private long LightmapResolutionScale = 1;
    private int LocalPlayerId = 255;

}
