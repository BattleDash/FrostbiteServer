package me.battledash.kyber.types.network.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.battledash.kyber.network.stream.managers.message.NetworkableMessage;
import me.battledash.kyber.network.stream.managers.message.StreamType;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.streams.BitStreamWrite;
import me.battledash.kyber.types.network.Initiator;
import me.battledash.kyber.types.network.NetworkableMessageMetadata;
import me.battledash.kyber.types.pojo.level.BundleHeapInfo;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@NetworkableMessageMetadata(hasNetworkedResources = false, initiator = Initiator.SERVER, messageStream = StreamType.GAME_RELIABLE)
public class SubLevelToClientLoadRequestsMessage extends NetworkableMessage {

    private long levelSequenceNumber;
    private boolean isBaselineMessage;
    private boolean skipWaitForBaseline;

    private List<SubLevelBundleInfo> infos = new ArrayList<>();

    @Override
    public boolean generatedReadFrom(BitStreamRead stream) {
        this.levelSequenceNumber = stream.readUnsigned(32);
        this.isBaselineMessage = stream.readBool();
        this.skipWaitForBaseline = stream.readBool();

        int size = (int) stream.readUnsigned(12);
        if (size > 2048) {
            return false;
        }
        this.infos.clear();
        for (int i = 0; i < size; i++) {
            SubLevelBundleInfo bundleInfo = new SubLevelBundleInfo();
            bundleInfo.setSubLevelNameInx((int) stream.readUnsigned(16));
            bundleInfo.setSubLevelId((int) stream.readUnsigned(16));
            bundleInfo.setParentSubLevelId((int) stream.readUnsigned(16));
            bundleInfo.setOwnerUniqueId((int) stream.readUnsigned(32));
            bundleInfo.setCompartmentIndex((int) stream.readUnsigned(32));
            bundleInfo.setPriority((short) stream.readUnsigned(8));
            bundleInfo.setPeerFiltered(stream.readBool());

            bundleInfo.setHeapInfo(BundleHeapInfo.readFrom(stream));
            bundleInfo.setBundleType(BundleType.readFrom(stream));
            bundleInfo.setBundleSettingsInfo(BundleSettingsInfo.readFrom(stream));
            this.infos.add(bundleInfo);
        }
        return true;
    }

    @Override
    public boolean generatedWriteTo(BitStreamWrite stream) {
        stream.writeUnsigned(this.levelSequenceNumber, 32);
        stream.writeBool(this.isBaselineMessage);
        stream.writeBool(this.skipWaitForBaseline);

        stream.writeUnsigned(this.infos.size(), 12);
        for (SubLevelBundleInfo info : this.infos) {
            stream.writeUnsigned(info.getSubLevelNameInx(), 16);
            stream.writeUnsigned(info.getSubLevelId(), 16);
            stream.writeUnsigned(info.getParentSubLevelId(), 16);
            stream.writeUnsigned(info.getOwnerUniqueId(), 32);
            stream.writeUnsigned(info.getCompartmentIndex(), 32);
            stream.writeUnsigned(info.getPriority(), 8);
            stream.writeBool(info.isPeerFiltered());

            info.getHeapInfo().writeTo(stream);
            info.getBundleType().writeTo(stream);
            info.getBundleSettingsInfo().writeTo(stream);
        }
        return true;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubLevelBundleInfo {
        private int subLevelNameInx;
        private int subLevelId;
        private int parentSubLevelId;
        private long ownerUniqueId;
        private int compartmentIndex;
        private short priority;
        private boolean isPeerFiltered;
        private boolean isTransientLaneContent;
        private BundleHeapInfo heapInfo;
        private BundleType bundleType;
        private BundleSettingsInfo bundleSettingsInfo;
    }

    public enum BundleType {
        SUB_LEVEL,
        BLUEPRINT_BUNDLE,
        SHARED_BUNDLE;

        private static final BundleType[] VALUES = BundleType.values();

        public static BundleType readFrom(BitStreamRead stream) {
            return BundleType.VALUES[(int) stream.readUnsignedLimit(0, 2)];
        }

        public void writeTo(BitStreamWrite stream) {
            stream.writeUnsignedLimit(this.ordinal(), 0, 2);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BundleSettingsInfo {
        private long groupIdentifier;
        private String groupName;

        public static BundleSettingsInfo readFrom(BitStreamRead stream) {
            BundleSettingsInfo info = new BundleSettingsInfo();
            info.setGroupIdentifier(stream.readUnsigned(32));
            info.setGroupName(stream.readString());
            return info;
        }

        public void writeTo(BitStreamWrite stream) {
            stream.writeUnsigned(this.groupIdentifier, 32);
            stream.writeString(this.groupName);
        }
    }

}
