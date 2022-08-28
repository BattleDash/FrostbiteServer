package me.battledash.kyber.types.pojo.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.types.pojo.Asset;

@Data
@EqualsAndHashCode(callSuper = true)
public class MixerEntityData extends EntityData implements EbxPOJO {

    private boolean ActivateOnCreation;
    private boolean AccumulatedInputs;
    private Asset Mixer;

}
