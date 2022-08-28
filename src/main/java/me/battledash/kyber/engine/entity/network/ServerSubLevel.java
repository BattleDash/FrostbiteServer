package me.battledash.kyber.engine.entity.network;

import me.battledash.kyber.engine.entity.SubLevel;
import me.battledash.kyber.types.pojo.Blueprint;

public class ServerSubLevel extends SubLevel {

    public ServerSubLevel(int uid, Blueprint data, SubLevel parent) {
        super(uid, data, parent);
        this.setGhost(new ServerSubLevelGhost(this, parent != null ? (ServerSubLevel) parent : null));
    }

}
