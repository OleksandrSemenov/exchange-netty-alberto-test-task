package com.alberto.matchingengine;

import com.alberto.matchingengine.server.OrderBook;
import com.alberto.matchingengine.server.ServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class MatchingEngineServer {

    private static final int PORT = 8080;

    // Create one order book instance (with preset resting orders)
    public static final OrderBook orderBook = new OrderBook();

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerInitializer());

            ChannelFuture f = b.bind(PORT).sync();
            log.info("Matching Engine Server started on port " + PORT);
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}