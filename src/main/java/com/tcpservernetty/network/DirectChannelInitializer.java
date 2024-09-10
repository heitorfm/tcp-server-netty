package com.tcpservernetty.network;

import com.tcpservernetty.utils.Validations;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.ArrayList;
import java.util.List;

public class DirectChannelInitializer extends ChannelInitializer<SocketChannel> {

    private List<HandlerConfig> handlers = new ArrayList<>();

    public void addHandler(ChannelHandler handler) {
        Validations.notNullArg(handler, "ChannelHandler can not be null");
        handlers.add(new HandlerConfig(null, null, handler));
    }

    public void addHandler(String name, ChannelHandler handler) {
        Validations.notNullArg(name, "name can not be null");
        Validations.notNullArg(handler, "ChannelHandler can not be null");
        handlers.add(new HandlerConfig(null, name, handler));
    }

    public void addHandler(EventExecutorGroup eventExecutors, String name, ChannelHandler handler) {
        Validations.notNullArg(eventExecutors, "eventExecutors can not be null");
        Validations.notNullArg(name, "name can not be null");
        Validations.notNullArg(handler, "ChannelHandler can not be null");
        handlers.add(new HandlerConfig(eventExecutors, name, handler));
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        ChannelPipeline pipeline = socketChannel.pipeline();

        for (HandlerConfig handlerConfig: handlers) {

            if(handlerConfig.eventExecutors != null && handlerConfig.name != null && handlerConfig.handler != null) {
                // adds handler with executor, name and the handler
                socketChannel.pipeline().addLast(handlerConfig.eventExecutors, handlerConfig.name, handlerConfig.handler);
            } else if(handlerConfig.name != null && handlerConfig.handler != null) {
                // adds handler *name* and the *handler*
                socketChannel.pipeline().addLast(handlerConfig.name, handlerConfig.handler);
            } else {
                socketChannel.pipeline().addLast(handlerConfig.handler);
            }

        }
    }

    // record to temporary store handler params
    private static record HandlerConfig(EventExecutorGroup eventExecutors, String name, ChannelHandler handler) {

    }

}
