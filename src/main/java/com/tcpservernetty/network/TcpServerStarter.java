package com.tcpservernetty.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TcpServerStarter {

    @Value("${tcp.server.port}")
    private int tcpServerPort;

    private Class<? extends ServerChannel> channelClazz;

    private static final Logger log = LoggerFactory.getLogger(TcpServerStarter.class);

    public EventLoopGroup createBossGroup() {
        EventLoopGroup worker = null;

        if (Epoll.isAvailable()) {
            this.channelClazz = EpollServerSocketChannel.class;
            worker = new EpollEventLoopGroup(2);

        } else {
            this.channelClazz = NioServerSocketChannel.class;
            worker = new NioEventLoopGroup(2);
        }

        return worker;
    }

    public int getEventExecutorThreadPoolSize() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        return (availableProcessors > 2) ? availableProcessors - 1 : availableProcessors;
    }

    public EventLoopGroup createWorkerGroup() {
        return (Epoll.isAvailable()) ? new EpollEventLoopGroup() : new NioEventLoopGroup();
    }

    public EventExecutorGroup eventExecutorGroup() {
        EventExecutorGroup eventExecutorGroup = null;
        int threadPoolSize = getEventExecutorThreadPoolSize();

        if (Epoll.isAvailable()) {
            eventExecutorGroup = new EpollEventLoopGroup(threadPoolSize, new DefaultThreadFactory("server-netty-worker-epoll", true));
        } else {
            eventExecutorGroup = new NioEventLoopGroup(threadPoolSize, new DefaultThreadFactory("server-netty-worker-nio", true));
        }

        return eventExecutorGroup;
    }

    @Bean
    public ChannelInitializer<SocketChannel> createChannelInitializer() {

        DirectChannelInitializer initializer = new DirectChannelInitializer();
        initializer.addHandler("size-header", new LengthFieldPrepender(2));
        initializer.addHandler(eventExecutorGroup(), "main-handler", new MessageHandler());

        return initializer;
    }

    @Bean
    public ServerBootstrap createBootstrap(ChannelInitializer<SocketChannel> channelInitializer) {

        EventLoopGroup bossGroup = createBossGroup();
        EventLoopGroup workerGroup = createWorkerGroup();

        ServerBootstrap server = new ServerBootstrap();
        server.group(bossGroup, workerGroup)
                .channel(channelClazz)
                //.handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(channelInitializer)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, false)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true);

        try {

            // Bind e comece a receber conexoes
            ChannelFuture channelFuture = server.bind(tcpServerPort);//.sync();

            // esperar ate o socket do servidor estar fechado
            //channelFuture.channel().closeFuture().sync();

        } catch(Exception ie) {
            log.error(ie.getMessage(), ie);
        }

        log.info("Server listening on port: {}", tcpServerPort);
        log.info("Event Executor Thread Pool Size: {}", getEventExecutorThreadPoolSize());

        return server;
    }


}
