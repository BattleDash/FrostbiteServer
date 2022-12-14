package me.battledash.kyber.network;

public enum TinyEvent {
    ACK_TIME_SYNC_DONE,
    ACK_LEVEL_LINKED,
    ACK_LEVEL_RESTARTED,
    ACK_ENTER_PATCH_RECV_STATE,
    ACK_AUTHENTICATION,
    NACK_AUTHENTICATION,
    ACK_EXIT_LEVEL,
    CMD_LINK_LEVEL,
    CMD_ENTER_PATCH_RECV_STATE,
    CMD_EXIT_LEVEL,
    CMD_CONTINUE_LEVEL,
    STAT_CONTROLLABLE_RUBBERBANDING,
    STAT_WORLD_RUBBERBANDING
}
