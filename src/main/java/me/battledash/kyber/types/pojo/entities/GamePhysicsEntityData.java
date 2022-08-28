package me.battledash.kyber.types.pojo.entities;

import com.google.gson.JsonObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
@EqualsAndHashCode(callSuper = true)
public class GamePhysicsEntityData extends GameComponentEntityData implements EbxPOJO {

    private JsonObject SourceModel;
    private JsonObject SourceGeoScene;
    private JsonObject PhysicsData;

}
