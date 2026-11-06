package com.github.kjetilv.uplift.synchttp.jmh;

import com.github.kjetilv.uplift.synchttp.HttpCallbackProcessor;
import com.github.kjetilv.uplift.synchttp.Server;
import com.github.kjetilv.uplift.synchttp.Servers;
import com.github.kjetilv.uplift.synchttp.rere.HttpReq;
import com.github.kjetilv.uplift.synchttp.write.HttpResponseCallback;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static java.nio.charset.StandardCharsets.UTF_8;

/// Measures the server side, with a raw socket client on one keep-alive connection.
///
/// The request has browser-like headers. The handler looks up headers and query parameters, and
/// responds with a few headers and a short body.
@Fork(1)
@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 5, time = 5)
@State(Scope.Benchmark)
public class RawSocketBenchmark {

    private static final byte[] RESPONSE_BODY = "{\"hello\":\"world\"}".getBytes(UTF_8);

    private static final byte[] REQUEST = ("""
        GET /api/items?user=1234&token=abcdef0123456789&album=xyz HTTP/1.1\r
        Host: localhost:8080\r
        User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0 Safari/537.36\r
        Accept: application/json, text/plain, */*\r
        Accept-Language: en-US,en;q=0.9,nb;q=0.8\r
        Accept-Encoding: gzip, deflate, br\r
        Origin: http://localhost:3000\r
        Referer: http://localhost:3000/albums?page=2\r
        Cookie: session=0123456789abcdef0123456789abcdef; theme=dark; consent=yes\r
        Sec-Fetch-Mode: cors\r
        Connection: keep-alive\r
        \r
        """).getBytes(UTF_8);

    private static final int PIPELINED = 16;

    private Server.Open server;

    @Setup(Level.Trial)
    public void setup() {
        server = Servers.create().open(new HttpCallbackProcessor(RawSocketBenchmark::handle));
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        server.close();
    }

    @Benchmark
    public int requestResponse(Connection connection) throws IOException {
        connection.out.write(REQUEST);
        connection.out.flush();
        return connection.readResponses(1);
    }

    @Benchmark
    @OperationsPerInvocation(PIPELINED)
    public int pipelined(Connection connection) throws IOException {
        connection.out.write(connection.pipelinedRequests);
        connection.out.flush();
        return connection.readResponses(PIPELINED);
    }

    private static void handle(HttpReq req, HttpResponseCallback callback) {
        var origin = req.origin();
        var accept = req.headers().header("accept");
        var params = req.queryParameters();
        if (accept == null || params.par("user") == null || params.par("token") == null) {
            callback.just(400);
            return;
        }
        callback.status(200)
            .contentType("application/json")
            .header("access-control-allow-origin", origin)
            .header("cache-control", "no-cache")
            .contentLength(RESPONSE_BODY.length)
            .body(RESPONSE_BODY);
    }

    @State(Scope.Thread)
    public static class Connection {

        private Socket socket;

        private OutputStream out;

        private InputStream in;

        private byte[] responseBuffer;

        private int responseLength;

        private byte[] pipelinedRequests;

        @Setup(Level.Trial)
        public void connect(RawSocketBenchmark benchmark) throws IOException {
            socket = new Socket();
            socket.setTcpNoDelay(true);
            socket.connect(benchmark.server.address());
            out = socket.getOutputStream();
            in = socket.getInputStream();
            pipelinedRequests = new byte[REQUEST.length * PIPELINED];
            for (var i = 0; i < PIPELINED; i++) {
                System.arraycopy(REQUEST, 0, pipelinedRequests, i * REQUEST.length, REQUEST.length);
            }
            responseLength = measureResponseLength();
            responseBuffer = new byte[responseLength * PIPELINED];
        }

        @TearDown(Level.Trial)
        public void close() throws IOException {
            socket.close();
        }

        int readResponses(int count) throws IOException {
            var expected = responseLength * count;
            var read = in.readNBytes(responseBuffer, 0, expected);
            if (read != expected) {
                throw new IllegalStateException("Expected " + expected + " bytes, got " + read);
            }
            return read;
        }

        /// Sends one request, and finds the response length from its headers
        private int measureResponseLength() throws IOException {
            out.write(REQUEST);
            out.flush();
            var head = new StringBuilder();
            while (!head.toString().endsWith("\r\n\r\n")) {
                var b = in.read();
                if (b < 0) {
                    throw new IllegalStateException("Connection closed: " + head);
                }
                head.append((char) b);
            }
            if (!head.toString().startsWith("HTTP/1.1 200")) {
                throw new IllegalStateException("Unexpected response: " + head);
            }
            var bodyLength = head.toString().lines()
                .filter(line -> line.toLowerCase().startsWith("content-length:"))
                .map(line -> Integer.parseInt(line.substring("content-length:".length()).strip()))
                .findFirst()
                .orElseThrow();
            in.readNBytes(bodyLength);
            return head.length() + bodyLength;
        }
    }
}
