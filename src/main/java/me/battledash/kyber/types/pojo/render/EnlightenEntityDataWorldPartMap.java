package me.battledash.kyber.types.pojo.render;

import lombok.Data;
import me.battledash.kyber.fs.ebx.EbxPOJO;

import java.util.UUID;

@Data
public class EnlightenEntityDataWorldPartMap implements EbxPOJO {

    private UUID EnlightenEntityDataId;
    private UUID WorldPartDataId;

}
