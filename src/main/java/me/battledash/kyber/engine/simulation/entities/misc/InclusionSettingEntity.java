package me.battledash.kyber.engine.simulation.entities.misc;

import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.entity.EntityCreationInfo;
import me.battledash.kyber.engine.entity.EntityEvent;
import me.battledash.kyber.engine.entity.EntityWithBusAndData;
import me.battledash.kyber.engine.entity.FrostbiteEntity;
import me.battledash.kyber.engine.simulation.LevelSetup;
import me.battledash.kyber.engine.simulation.level.Level;
import me.battledash.kyber.server.ServerGameContext;
import me.battledash.kyber.types.pojo.entities.InclusionSettingEntityData;

@Slf4j
@FrostbiteEntity(InclusionSettingEntityData.class)
public class InclusionSettingEntity extends EntityWithBusAndData<InclusionSettingEntityData> {

    public InclusionSettingEntity(EntityCreationInfo info, InclusionSettingEntityData data) {
        super(info, data);
        boolean boolValue = this.isSettingValid();
        // TODO: 5/11/2022 Handle PropertyWriter
    }

    @Override
    public void onCreate(EntityCreationInfo info) {
    }

    @Override
    public void event(EntityEvent event) {
    }

    private boolean isSettingValid() {
        Level level = ServerGameContext.context().getLevel();
        if (level != null) {
            LevelSetup setup = level.getSetup();
            String setting = this.getData().getSetting();
            String currentOption = setup.getInclusionOption(setting);

            String[] settings = this.getData().getSettings();
            for (String s : settings) {
                if (currentOption.equals(setup.getInclusionOption(s))) {
                    return true;
                }
            }
        }
        return false;
    }

}