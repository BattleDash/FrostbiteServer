package me.battledash.kyber.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.streams.InBitStream;
import me.battledash.kyber.network.packet.FrostbitePacket;
import me.battledash.kyber.network.packet.CommandType;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class PacketDecoder extends MessageToMessageDecoder<DatagramPacket> {

    private static final CommandType[] COMMAND_TYPES = CommandType.values();

    public static final int COMMAND_BITS = 4;
    public static final int PADDING_BITS = 3;
    public static final int CHANNEL_BITS = 14;

    public static final int HEADER_SIZE =
            PacketDecoder.COMMAND_BITS +
            PacketDecoder.PADDING_BITS +
            PacketDecoder.CHANNEL_BITS;

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) {
        FrostbitePacket<InBitStream> packet = this.decodePacket(msg.content(), msg.sender());
        if (packet == null) {
            return;
        }
        out.add(packet);
    }

    public FrostbitePacket<InBitStream> decodePacket(ByteBuf content, InetSocketAddress sender) {
        log.debug("Raw packet: {}", ByteBufUtil.hexDump(content));

        InBitStream inStream = new InBitStream();
        inStream.initBits(content, PacketDecoder.PADDING_BITS);
        byte bitsInLastOctet = (byte) inStream.read(PacketDecoder.PADDING_BITS);

        int bits = (content.writerIndex() - 1) * 8 + bitsInLastOctet;
        if (bitsInLastOctet == 0) {
            bits += 8;
        }

        if (bits < PacketDecoder.HEADER_SIZE) {
            log.warn("Packet is too small ({} bits)", bits);
            return null;
        }

        log.debug("Packet is {} bits long and has {} bits in the last octet, with {} readable bytes", bits, bitsInLastOctet, content.readableBytes());

        inStream.initBits(content, bits);
        inStream.seek(PacketDecoder.PADDING_BITS);

        int channelId = (int) inStream.read(PacketDecoder.CHANNEL_BITS);
        CommandType cmd = PacketDecoder.COMMAND_TYPES[(int) (inStream.read(PacketDecoder.COMMAND_BITS) - 1)];

        FrostbitePacket<InBitStream> packet = new FrostbitePacket<>(channelId, cmd, inStream, sender);
        packet.setOriginal(content.duplicate());
        return packet;
    }

}
