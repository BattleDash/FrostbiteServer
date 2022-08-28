package me.battledash.kyber.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.streams.OutBitStream;
import me.battledash.kyber.network.packet.FrostbitePacket;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class PacketEncoder extends MessageToMessageEncoder<FrostbitePacket<OutBitStream>> {

    @Override
    protected void encode(ChannelHandlerContext ctx, FrostbitePacket<OutBitStream> packet, List<Object> out) {
        OutBitStream data = OutBitStream.init();
        data.write(0, PacketDecoder.PADDING_BITS);
        data.write(packet.getChannelID(), PacketDecoder.CHANNEL_BITS);
        data.write(packet.getCommand().ordinal() + 1, PacketDecoder.COMMAND_BITS);

        try {
            OutBitStream packetData = packet.getData();
            if (packetData != null) {
                packetData.flush();
                data.writeStream(packetData.convertToRead());
            }
        } catch (Exception e) {
            log.error("Error while encoding packet: {}", packet, e);
        }

        int pos = data.getStreamBitWritePosition();
        data.seek(0);
        data.write(pos & 7, PacketDecoder.PADDING_BITS);
        data.seek(pos);
        data.flush();

        ByteBuf finalBuf = data.getBuffer().retain().copy(0, data.getOctetCount());
        //BufferUtil.setUnsignedShortLE(finalBuf, (int) PacketHandler.DATA_HEADER_SIZE, BufferUtil.calcFletcher16(0, finalBuf));

        log.debug("Raw packet: {}", ByteBufUtil.hexDump(finalBuf));

        try {
            ctx.channel().writeAndFlush(new DatagramPacket(finalBuf, packet.getAddress(),
                    new InetSocketAddress("0.0.0.0", 25200)));
            log.debug("Encoded packet {} and sent to {} ({})", packet.getCommand(), packet.getAddress(), data.getOctetCount());
        } catch (Exception e) {
            log.error("Error while sending packet: {}", packet, e);
        }
    }

}
