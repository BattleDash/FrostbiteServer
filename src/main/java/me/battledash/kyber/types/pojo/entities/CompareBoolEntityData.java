package me.battledash.kyber.types.pojo.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
@EqualsAndHashCode(callSuper = true)
public class CompareBoolEntityData extends CompareEntityBaseData implements EbxPOJO {

    // Boxed type to force lombok to use "get" instead of "is"
    private Boolean Bool;

}
