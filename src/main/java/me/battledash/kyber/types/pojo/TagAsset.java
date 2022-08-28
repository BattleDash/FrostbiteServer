package me.battledash.kyber.types.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
@EqualsAndHashCode(callSuper = true)
public class TagAsset extends Asset implements EbxPOJO {

    private String Description;
    private TagAsset Parent;

}
