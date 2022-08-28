package me.battledash.kyber.types.pojo.level;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.types.pojo.Asset;

@Data
@EqualsAndHashCode(callSuper = true)
public class SubWorldInclusion extends Asset implements EbxPOJO {

    private SubWorldInclusionCriterion[] Criteria;

}
