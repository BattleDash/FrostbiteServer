package me.battledash.kyber.types.pojo.level;

import lombok.Data;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
public class SubWorldInclusionSettings implements EbxPOJO {

    private SubWorldInclusionSetting[] Settings;

}
