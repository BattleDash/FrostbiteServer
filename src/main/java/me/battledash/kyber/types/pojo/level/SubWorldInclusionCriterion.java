package me.battledash.kyber.types.pojo.level;

import lombok.Data;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
public class SubWorldInclusionCriterion implements EbxPOJO {

    private String Name;
    private String[] Options;

}
