package me.battledash.kyber.types.pojo.level;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.streams.BitStreamWrite;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BundleHeapInfo implements EbxPOJO {
    private static final BundleHeapType[] HEAP_TYPES = BundleHeapType.values();

    private BundleHeapType HeapType = BundleHeapType.PARENT;
    private long InitialSize;
    private boolean AllowGrow = true;

    public static BundleHeapInfo readFrom(BitStreamRead stream) {
        BundleHeapInfo heapInfo = new BundleHeapInfo();
        heapInfo.setHeapType(BundleHeapInfo.HEAP_TYPES[(int) stream.readUnsignedLimit(0, 6)]);
        heapInfo.setInitialSize(stream.readUnsigned(32));
        heapInfo.setAllowGrow(stream.readBool());
        return heapInfo;
    }

    public void writeTo(BitStreamWrite stream) {
        stream.writeUnsignedLimit(this.HeapType.ordinal(), 0, 6);
        stream.writeUnsigned(this.InitialSize, 32);
        stream.writeBool(this.AllowGrow);
    }

    public enum BundleHeapType {
        OWN_WITH_PARENT_SMALLBLOCK,
        OWN_WITH_SMALLBLOCK,
        OWN_WITHOUT_SMALLBLOCK,
        PARENT,
        LEVEL,
        GLOBAL,
        NULL
    }
}