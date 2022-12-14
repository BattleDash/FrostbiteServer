package me.battledash.kyber.network.packet;

public enum CommandType {
    CHALLENGE_RESPONSE,
    CONNECT,
    REJECT,
    ACCEPT,
    ACCEPT_CONFIRMATION,
    CONNECTION_CONFIRMED,
    HOST_INFO_REQUEST,
    HOST_INFO,
    BUSY,
    CHALLENGE,
    DISCONNECT,
    CUSTOM_CHALLENGE,
    CUSTOM_CHALLENGE_RESPONSE,
    REMOTE_DISCONNECT,
    DATA
}
