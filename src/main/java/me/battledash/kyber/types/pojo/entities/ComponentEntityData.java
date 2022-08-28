package me.battledash.kyber.types.pojo.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.types.pojo.GameObjectData;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
@EqualsAndHashCode(callSuper = true)
public class ComponentEntityData extends SpatialEntityData implements EbxPOJO {

    private boolean IsExposedToSubLevels;
    private GameObjectData[] Components;
    private GameObjectData[] PartBoundingBoxes;
    private short ClientRuntimeComponentCount;
    private short ServerRuntimeComponentCount;
    private short ClientRuntimeTransformationCount;
    private short ServerRuntimeTransformationCount;

}
