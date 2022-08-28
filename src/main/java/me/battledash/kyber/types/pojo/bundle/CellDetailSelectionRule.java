package me.battledash.kyber.types.pojo.bundle;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
@EqualsAndHashCode(callSuper = true)
public class CellDetailSelectionRule extends PackagingRule implements EbxPOJO {

    private PackagingDetailLevel Detail;

    private PackagingDetailInfo DetailInfoWin32;
    private PackagingDetailInfo DetailInfoGen4a;
    private PackagingDetailInfo DetailInfoGen4b;

    public enum PackagingDetailLevel {
        LOW,
        MEDIUM,
        HIGH,
        BASE
    }

}
