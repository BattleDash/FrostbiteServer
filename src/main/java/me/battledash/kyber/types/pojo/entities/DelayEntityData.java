package me.battledash.kyber.types.pojo.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.types.Realm;
import me.battledash.kyber.types.pojo.EntityBusData;

@Data
@EqualsAndHashCode(callSuper = true)
public class DelayEntityData extends EntityData implements EbxPOJO {

    private float Delay;
    private Realm Realm;
    private EntityBusData.TimeDeltaType TimeDeltaType;
    private boolean AutoStart;
    private boolean RunOnce;
    private boolean RemoveDuplicateEvents;

}
