package me.battledash.kyber.misc;

import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class Buffer {

    private String name;
    private int size;
    private ByteBuf data;

}
