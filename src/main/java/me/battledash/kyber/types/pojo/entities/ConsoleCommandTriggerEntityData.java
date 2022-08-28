package me.battledash.kyber.types.pojo.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.fs.ebx.EbxPOJO;
import me.battledash.kyber.fs.ebx.NotifyDeserialized;
import me.battledash.kyber.types.Realm;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class ConsoleCommandTriggerEntityData extends EntityData implements EbxPOJO, NotifyDeserialized {

    private String CommandName;
    private String[] Arguments;
    private String GroupName;
    private Realm Realm;
    private boolean UpdateDefaultsOnChanged;

    @Override
    public void onDeserialized() {
        log.info("{}", this);
    }

}
