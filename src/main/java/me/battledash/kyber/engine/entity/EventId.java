package me.battledash.kyber.engine.entity;

import lombok.Data;
import me.battledash.kyber.server.ServerGameContext;
import me.battledash.kyber.util.Fnv1;

@Data
public class EventId {

    private String name;
    private long id;

    public EventId(String name) {
        this.name = name;
        this.id = Fnv1.hashString(name);
    }

    public EventId(long id) {
        this.id = id;
        this.name = ServerGameContext.context().getServer().getAssetManager().getHashedString(id);
    }

}
