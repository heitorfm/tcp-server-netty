package com.tcpservernetty.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@ChannelHandler.Sharable
public class MessageHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(TcpServerStarter.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;

        byte[] payload = new byte[in.readableBytes()];
        in.readBytes(payload);

        //log.info("Received: {}", new String(payload));

        String host = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress();

        String ret = "client: " + host + "\n" +
                "time: " + LocalDateTime.now().toString();

        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(ret.length());
        buffer.writeBytes(ret.getBytes(StandardCharsets.UTF_8));

        ChannelFuture channelFuture = ctx.writeAndFlush(buffer);

        channelFuture.addListeners(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) {

                if (future.isSuccess()) {
                    //log.info("Mensagem enviada com sucesso.");
                } else {
                    Throwable cause = future.cause();
                    log.error(cause.getMessage(), cause);
                }
            }

        });

        ctx.close();

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
        // Fechar a conexao quando surgir um erro
        ctx.close();
    }

}
