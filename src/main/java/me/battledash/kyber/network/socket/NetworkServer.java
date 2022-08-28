package me.battledash.kyber.network.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.Tickable;
import me.battledash.kyber.network.ConnectionManager;
import me.battledash.kyber.network.PacketDecoder;
import me.battledash.kyber.network.PacketHandler;
import me.battledash.kyber.network.PacketEncoder;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnixDomainSocketAddress;

@Slf4j
@Getter
public class NetworkServer implements Tickable {

    private final ChannelGroup channelGroup;
    private final SocketAddress socketAddress;
    private String address;
    private int port;
    private PacketHandler packetHandler;

    public NetworkServer(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
        if (socketAddress instanceof InetSocketAddress inetSocketAddress) {
            this.address = inetSocketAddress.getAddress().getHostAddress();
            this.port = inetSocketAddress.getPort();
        } else if (socketAddress instanceof UnixDomainSocketAddress unixDomainSocketAddress) {
            this.address = unixDomainSocketAddress.getPath().getFileName().toString();
            this.port = 0;
        }
        this.channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    public void listen() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.AUTO_CLOSE, true)
                .option(ChannelOption.SO_BROADCAST, true)
                .option(ChannelOption.SO_REUSEADDR, true);

        PacketHandler handler = this.packetHandler = new PacketHandler(new ConnectionManager());

        bootstrap.handler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(@NonNull Channel ch) {
                ch.pipeline()
                        .addLast("decoder", new PacketDecoder())
                        .addLast("encoder", new PacketEncoder())
                        .addLast(new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors()),
                                "handler", handler);
            }
        });

        try {
            ChannelFuture channelFuture = bootstrap.bind(this.port).sync();
            this.channelGroup.add(channelFuture.channel());
        } catch (InterruptedException e) {
            log.error("Failed to bind server", e);
            System.exit(1);
        }
    }

    public void tick() {
        this.packetHandler.tick();
    }

    public void stop() {
        this.channelGroup.close().awaitUninterruptibly();
    }

}
