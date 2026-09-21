package com.github.kjetilv.uplift.synchttp.jmh;

import com.github.kjetilv.uplift.synchttp.HttpCallbackProcessor;
import com.github.kjetilv.uplift.synchttp.Server;
import com.github.kjetilv.uplift.synchttp.rere.HttpReq;
import com.github.kjetilv.uplift.synchttp.write.HttpResponseCallback;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import org.openjdk.jmh.annotations.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.nio.charset.StandardCharsets.UTF_8;

@Fork(1)
@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 5, time = 5)
@State(Scope.Benchmark)
public class ComprehensiveHttpBenchmark {

    @Param
    private Scenario scenario;

    private Server upliftServer;

    private Channel nettyChannel;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private HttpClient httpClient;

    private HttpRequest upliftRequest;

    private HttpRequest nettyRequest;

    @Setup(Level.Trial)
    public void setup() {
        upliftServer = Server.create().run(
            new HttpCallbackProcessor(ComprehensiveHttpBenchmark::handleUplift));

        setupNetty();

        var nettyPort = ((InetSocketAddress) nettyChannel.localAddress()).getPort();
        httpClient = HttpClient.newHttpClient();
        upliftRequest = buildRequest(upliftServer.uri(), scenario);
        nettyRequest = buildRequest(URI.create("http://127.0.0.1:" + nettyPort), scenario);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        httpClient.close();
        upliftServer.close();
        nettyChannel.close();
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    @Benchmark
    public HttpResponse<byte[]> uplift() throws Exception {
        return httpClient.send(upliftRequest, HttpResponse.BodyHandlers.ofByteArray());
    }

    @Benchmark
    public HttpResponse<byte[]> netty() throws Exception {
        return httpClient.send(nettyRequest, HttpResponse.BodyHandlers.ofByteArray());
    }

    private void setupNetty() {
        var ioHandlerFactory = NioIoHandler.newFactory();
        bossGroup = new MultiThreadIoEventLoopGroup(ioHandlerFactory);
        workerGroup = new MultiThreadIoEventLoopGroup(ioHandlerFactory);
        try {
            nettyChannel = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                            .addLast(new HttpServerCodec())
                            .addLast(new HttpObjectAggregator(65536))
                            .addLast(new RoutingHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .bind(0)
                .sync()
                .channel();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted during server start", e);
        }
    }

    private static final byte[] REQUEST_BODY =
        // language=json
        """
            {"name":"benchmark","value":42}
            """.getBytes(UTF_8);

    private static final byte[] RESPONSE_BODY = "Hello, World!".getBytes(UTF_8);

    private static void handleUplift(HttpReq req, HttpResponseCallback callback) {
        var contentLength = req.headers().header(HttpReq.CONTENT_LENGTH);
        if (contentLength != null && !contentLength.isEmpty()) {
            req.bodyBytes();
        }
        var path = req.path();
        if (path.indexOf('?') >= 0) {
            req.queryParameters();
        }
        if (path.startsWith("/b")) {
            callback.status(200)
                .contentType("text/plain; charset=UTF-8")
                .contentLength(RESPONSE_BODY.length)
                .body(RESPONSE_BODY);
        } else {
            callback.status(204).nobody();
        }
    }

    private static HttpRequest buildRequest(URI baseUri, Scenario scenario) {
        var uri = baseUri.resolve(scenario.path);
        var bodyPublisher = scenario.hasRequestBody
            ? HttpRequest.BodyPublishers.ofByteArray(REQUEST_BODY)
            : HttpRequest.BodyPublishers.noBody();
        var builder = HttpRequest.newBuilder(uri)
            .method(scenario.method, bodyPublisher);
        if (scenario.hasRequestBody) {
            builder.header("Content-Type", "application/json");
        }
        return builder.build();
    }

    private static final class RoutingHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
            var content = msg.content();
            if (content.readableBytes() > 0) {
                var bytes = new byte[content.readableBytes()];
                content.readBytes(bytes);
            }
            var uri = msg.uri();
            if (uri.indexOf('?') >= 0) {
                new QueryStringDecoder(uri).parameters();
            }
            if (uri.startsWith("/b")) {
                var response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(RESPONSE_BODY)
                );
                response.headers()
                    .set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
                    .setInt(HttpHeaderNames.CONTENT_LENGTH, RESPONSE_BODY.length);
                ctx.writeAndFlush(response);
            } else {
                var response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.NO_CONTENT
                );
                response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, 0);
                ctx.writeAndFlush(response);
            }
        }
    }

    public enum Scenario {

        GET_EMPTY("/empty", "GET", false),
        GET_BODY_OUT("/body", "GET", false),
        GET_EMPTY_QP("/empty?foo=bar&baz=qux", "GET", false),
        GET_BODY_OUT_QP("/body?foo=bar&baz=qux", "GET", false),
        POST_BODY_IN_EMPTY("/empty", "POST", true),
        POST_BODY_IN_BODY_OUT("/body", "POST", true),
        POST_BODY_IN_BODY_OUT_QP("/body?foo=bar&baz=qux", "POST", true),
        PUT_BODY_IN_EMPTY("/empty", "PUT", true),
        PUT_BODY_IN_BODY_OUT("/body", "PUT", true),
        DELETE_EMPTY("/empty", "DELETE", false),
        DELETE_BODY_OUT("/body", "DELETE", false),
        DELETE_EMPTY_QP("/empty?id=42", "DELETE", false);

        final String path;

        final String method;

        final boolean hasRequestBody;

        Scenario(String path, String method, boolean hasRequestBody) {
            this.path = path;
            this.method = method;
            this.hasRequestBody = hasRequestBody;
        }
    }
}
