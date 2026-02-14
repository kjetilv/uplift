package com.github.kjetilv.uplift.synchttp.jmh;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;

import java.net.InetSocketAddress;

import static java.nio.charset.StandardCharsets.UTF_8;

final class NettyServer implements AutoCloseable {

    private final EventLoopGroup bossGroup;

    private final EventLoopGroup workerGroup;

    private final Channel channel;

    private final int port;

    NettyServer(byte[] responseBody) {
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
        try {
            var bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                            .addLast(new HttpServerCodec())
                            .addLast(new HttpObjectAggregator(65536))
                            .addLast(new NettyHandler(responseBody));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

            this.channel = bootstrap.bind(0).sync().channel();
            this.port = ((InetSocketAddress) channel.localAddress()).getPort();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted during server start", e);
        }
    }

    int port() {
        return port;
    }

    @Override
    public void close() {
        channel.close();
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    private static final class NettyHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        private final byte[] responseBody;

        NettyHandler(byte[] responseBody) {
            this.responseBody = responseBody;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
            var response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(responseBody)
            );
            response.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
                .setInt(HttpHeaderNames.CONTENT_LENGTH, responseBody.length);
            ctx.writeAndFlush(response);
        }
    }
}
