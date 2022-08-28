package me.battledash.kyber.types.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
@EqualsAndHashCode(callSuper = true)
public class GameObjectData extends DataBusPeer implements EbxPOJO {

    private boolean IsGhost;
    private boolean IsEntityOwner;
    private boolean ForceLinked;
    private boolean SupportsGameView;

}
