package me.battledash.kyber.engine.server.level;

public enum ServerLoadState {
    LOADING,
    INITIALIZING,
    WAITING_FOR_SUBLEVEL_DATA,
    SPAWNING_ENTITIES,
    FINALIZING,
    DONE,
    ABORTING,
    ABORTED
}
