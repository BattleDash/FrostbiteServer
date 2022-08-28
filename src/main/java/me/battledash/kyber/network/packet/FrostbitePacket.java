package me.battledash.kyber.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.Data;

import java.net.InetSocketAddress;

@Data
public class FrostbitePacket<T> {
    private final int channelID;
    private final CommandType command;
    private final T data;
    private final InetSocketAddress address;
    private ByteBuf original;
    private Object extra;
}
