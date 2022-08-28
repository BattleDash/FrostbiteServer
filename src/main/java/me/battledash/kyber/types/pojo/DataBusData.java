package me.battledash.kyber.types.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
@EqualsAndHashCode(callSuper = true)
public class DataBusData extends Asset implements EbxPOJO {

    public static final int USED_FLAG_COUNT = 0;

    private int Flags;
    private PropertyConnection[] PropertyConnections;
    private LinkConnection[] LinkConnections;
    private DynamicDataContainer Interface;

}
