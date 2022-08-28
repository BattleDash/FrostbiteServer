package me.battledash.kyber.types.pojo.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.types.pojo.Asset;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocalPlayerEntityData extends EntityData implements EbxPOJO {

    private int LocalPlayerId;
    private boolean UpdatePosition;
    private boolean UpdateIsDamageable;
    private boolean UpdatePendingWeaponSlots;
    private boolean AlwaysTriggerOnWeaponEntry;

}
