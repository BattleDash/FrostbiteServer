package me.battledash.kyber.engine.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import me.battledash.kyber.streams.BitStreamRead;
import me.battledash.kyber.streams.BitStreamWrite;
import me.battledash.kyber.types.pojo.Blueprint;
import me.battledash.kyber.types.pojo.level.SubWorldData;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SubLevel extends SimpleEntityOwner {

    private Blueprint subLevelData;
    private EntityBus rootEntityBus;
    private String name;
    private int subLevelId; // 0 = Main, 1 = Sub1, 2 = Sub2, etc.
    private int uid;

    public SubLevel(int uid, Blueprint data, SubLevel parent) {
        super(parent);
        this.subLevelData = data;
        this.uid = uid;
    }

    public SubLevel getParent() {
        return super.getParent() instanceof SubLevel ? (SubLevel) super.getParent() : null;
    }

    public String getName() {
        return this.subLevelData != null ? this.subLevelData.getName() : "<SubLevel>";
    }

    public static int readSubLevelId(BitStreamRead stream) {
        return (int) stream.readUnsignedLimit(0, 65535);
    }

    public static void writeSubLevelId(BitStreamWrite stream, int subLevelId) {
        stream.writeUnsignedLimit(subLevelId, 0, 65535);
    }

    public boolean isBlueprintBundle() {
        return this.subLevelData != null && !(this.subLevelData instanceof SubWorldData);
    }

    @Override
    public Integer internalGetUniqueId() {
        return this.uid;
    }

}
