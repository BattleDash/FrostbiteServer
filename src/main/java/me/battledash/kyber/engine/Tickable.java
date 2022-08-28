package me.battledash.kyber.engine;

import org.slf4j.LoggerFactory;

public interface Tickable {

    default void tick() {
        LoggerFactory.getLogger(this.getClass()).warn("An unhandled tick was caught, please override the tick method or stop implementing Tickable");
    }

    default void tick(int deltaTick) {
        this.tick();
    }

}
