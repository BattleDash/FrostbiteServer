package me.battledash.kyber.types.pojo.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
@EqualsAndHashCode(callSuper = true)
public class GameComponentEntityData extends ComponentEntityData implements EbxPOJO {

    private boolean Enabled;
    private boolean EnableOutOfBoundsCheck = true;

}
