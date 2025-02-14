package com.alberto.matchingengine;

import com.alberto.matchingengine.client.ClientInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Scanner;

public class MatchingEngineClient {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("Enter the matching engine server host:");
        //read host and port from System.in
        Scanner scanner = new Scanner(System.in);
        String host = scanner.nextLine();
        System.out.println("Enter the matching engine server port:");
        int port = Integer.parseInt(scanner.nextLine());

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientInitializer());

            // Connect to the matching engine server.
            Channel ch = b.connect(host, port).sync().channel();

            // Read orders from the console.
            scanner = new Scanner(System.in);
            System.out.println("Enter MARKET orders in JSON format (or type 'quit' to exit):");
            while (true) {
                String line = scanner.nextLine();
                if ("quit".equalsIgnoreCase(line)) {
                    break;
                }
                // Append newline as a message delimiter.
                ch.writeAndFlush(line + "\n");
            }
        } finally {
            group.shutdownGracefully();
        }
    }

}
