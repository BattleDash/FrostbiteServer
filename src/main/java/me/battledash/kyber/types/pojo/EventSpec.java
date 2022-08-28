package me.battledash.kyber.types.pojo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.fs.ebx.NotifyDeserialized;
import me.battledash.kyber.server.ServerGameContext;

import java.util.Map;

@Data
@Slf4j
public class EventSpec implements EbxPOJO, NotifyDeserialized {

    private String Name;
    private int Id;

    @Override
    public void onDeserialized() {
        this.Name = ServerGameContext.context().getServer().getAssetManager().getHashedString(this.Id);
    }

}
