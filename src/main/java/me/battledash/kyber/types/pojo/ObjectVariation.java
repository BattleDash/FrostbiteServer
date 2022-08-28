package me.battledash.kyber.types.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
@EqualsAndHashCode(callSuper = true)
public class ObjectVariation extends Asset implements EbxPOJO {

    private boolean IsNetworkedResource = true;
    private long NameHash;
    private BlueprintVariation[] BlueprintVariations;
    private Object Disable;

    public static class BaseObjectVariationData implements EbxPOJO {
    }

    @Data
    public static class BlueprintVariation implements EbxPOJO {
        private Blueprint Blueprint;
        private BaseObjectVariationData[] Variations;
    }

}
