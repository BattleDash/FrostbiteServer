package me.battledash.kyber.types.pojo;

import lombok.Data;
import me.battledash.kyber.fs.ebx.EbxPOJO;

@Data
public class EventConnection implements EbxPOJO {

    private Object Source;
    private Object Target;

    private EventSpec SourceEvent;
    private EventSpec TargetEvent;

    private EventConnectionTargetType TargetType = EventConnectionTargetType.INVALID;

    public enum EventConnectionTargetType {
        INVALID,
        CLIENT_AND_SERVER,
        CLIENT,
        SERVER,
        NETWORKED_CLIENT,
        NETWORKED_CLIENT_AND_SERVER
    }

}
