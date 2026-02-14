package com.github.kjetilv.uplift.synchttp.jmh;

import com.github.kjetilv.uplift.synchttp.HttpCallbackProcessor;
import com.github.kjetilv.uplift.synchttp.Server;
import org.openjdk.jmh.annotations.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.nio.charset.StandardCharsets.UTF_8;

@Fork(1)
@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 5, time = 5)
@State(Scope.Benchmark)
public class HttpBenchmark {

    private static final byte[] RESPONSE_BODY = "Hello, World!".getBytes(UTF_8);

    private Server upliftServer;

    private NettyServer nettyServer;

    private HttpClient httpClient;

    private URI upliftUri;

    private URI nettyUri;

    @Setup(Level.Trial)
    public void setup() {
        var handler = new HttpCallbackProcessor((_, callback) ->
            callback.status(200)
                .contentType("text/plain; charset=UTF-8")
                .contentLength(RESPONSE_BODY.length)
                .body(RESPONSE_BODY));
        upliftServer = Server.create().run(handler);
        upliftUri = upliftServer.uri();

        nettyServer = new NettyServer(RESPONSE_BODY);
        nettyUri = URI.create("http://127.0.0.1:" + nettyServer.port());

        httpClient = HttpClient.newHttpClient();
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        httpClient.close();
        upliftServer.close();
        nettyServer.close();
    }

    @Benchmark
    public HttpResponse<String> upliftSynchttp() throws Exception {
        return httpClient.send(
            HttpRequest.newBuilder(upliftUri).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );
    }

    @Benchmark
    public HttpResponse<String> netty() throws Exception {
        return httpClient.send(
            HttpRequest.newBuilder(nettyUri).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );
    }
}
