package me.battledash.kyber.types.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.fs.ebx.EbxType;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class TemplateAsset extends Asset implements EbxPOJO {

    private UUID TargetAssetRef;
    private EbxType<?> TargetAssetType;
    private Asset TargetAsset;
    private TemplateAsset[] Contributors;
    private TemplateTarget[] Objects;

    public static class Template implements EbxPOJO {
    }

    public static class TemplateTarget implements EbxPOJO {
        private UUID TargetRef;
        private Object Target;
        private Template Template;
    }

}
