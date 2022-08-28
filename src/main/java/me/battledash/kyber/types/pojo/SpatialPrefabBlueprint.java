package me.battledash.kyber.types.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpatialPrefabBlueprint extends PrefabBlueprint implements EbxPOJO {

    private boolean CreateGroupEntity = false;
    private boolean GroupSupportsKeyframing = false;
    private boolean SecondaryLane = false;
    private boolean PreserveScaling = false;

}
