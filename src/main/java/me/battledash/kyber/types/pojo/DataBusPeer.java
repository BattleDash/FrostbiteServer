package me.battledash.kyber.types.pojo;

import lombok.Getter;
import me.battledash.kyber.fs.ebx.EbxPOJO;

public class DataBusPeer extends GameDataContainer implements EbxPOJO {

    @Getter
    private long Flags;

}
