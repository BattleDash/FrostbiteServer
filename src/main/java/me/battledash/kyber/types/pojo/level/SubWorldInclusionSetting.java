package me.battledash.kyber.types.pojo.level;

import lombok.Data;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
public class SubWorldInclusionSetting implements EbxPOJO {

    private SubWorldInclusionCriterion Criterion;
    private String[] EnabledOptions;

}
