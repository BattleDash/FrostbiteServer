package me.battledash.kyber.types.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.engine.entity.EntityBus;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
@EqualsAndHashCode(callSuper = true)
public class EntityBusData extends DataBusData implements EbxPOJO {

    private static final int FIRST_FREE_FLAG = DataBusData.USED_FLAG_COUNT;
    private static final long NEED_NETWORK_ID_FLAG = 1L << EntityBusData.FIRST_FREE_FLAG;

    private GameObjectData[] SchematicGroups;
    private GameObjectData[] SchematicAnnotations;
    private GameObjectData[] SchematicShortcuts;

    @SerializedName("TimeDeltaType")
    private TimeDeltaType timeDeltaType = EntityBusData.TimeDeltaType.NONE;

    private EventConnection[] EventConnections;

    private boolean ForceNetworked = false;

    public boolean getNeedNetworkId() {
        return (this.getFlags() & EntityBusData.NEED_NETWORK_ID_FLAG) != 0;
    }

    public enum TimeDeltaType {
        NONE,
        PLAYER,
        WORLD,
        EXTRA1,
        EXTRA2,
        EXTRA3,
        EXTRA4,
        EXTRA5,
        COUNT
    }

}
