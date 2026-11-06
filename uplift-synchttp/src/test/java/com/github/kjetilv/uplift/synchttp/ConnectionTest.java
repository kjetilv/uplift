package com.github.kjetilv.uplift.synchttp;

import module java.base;
import com.github.kjetilv.uplift.synchttp.rere.HttpReq;
import com.github.kjetilv.uplift.synchttp.write.HttpResponseCallback;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/// Tests for connection handling: keep-alive, pipelining, body limits, handler failures and concurrency.
class ConnectionTest {

    @Test
    void pipelinedRequestsAllGetResponses() {
        var output = withServer(
            ConnectionTest::echo,
            server -> halfClosedExchange(
                server,
                get("/one") + get("/two") + get("/three")
            )
        );
        assertThat(output).isEqualTo(ok("GET /one") + ok("GET /two") + ok("GET /three"));
    }

    @Test
    void requestsInSeparateWritesOnOneConnection() {
        var output = withServer(
            ConnectionTest::echo,
            server -> halfClosedExchange(server, get("/one"), get("/two"))
        );
        assertThat(output).isEqualTo(ok("GET /one") + ok("GET /two"));
    }

    @Test
    void bodyIsLimitedToContentLength() {
        var output = withServer(
            (req, callback) -> {
                var body = req.method() + " " + req.path() + " " + req.bodyString();
                callback.status(200).contentLength(body.length()).body(body);
            },
            server -> halfClosedExchange(
                server,
                post("/p", "0123456789") + get("/after")
            )
        );
        assertThat(output).isEqualTo(ok("POST /p 0123456789") + ok("GET /after "));
    }

    @Test
    void bodyReadThroughChannelIsLimitedToContentLength() {
        var output = withServer(
            (req, callback) -> {
                String body;
                try {
                    body = req.method() + " " + req.path() + " " + new String(req.bodyStream().readAllBytes(), UTF_8);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                callback.status(200).contentLength(body.length()).body(body);
            },
            server -> halfClosedExchange(
                server,
                post("/p", "0123456789") + get("/after")
            )
        );
        assertThat(output).isEqualTo(ok("POST /p 0123456789") + ok("GET /after "));
    }

    @Test
    void unreadBodyInSameWriteIsSkipped() {
        var output = withServer(
            ConnectionTest::echo,
            server -> halfClosedExchange(
                server,
                post("/p", "GET /smuggled HTTP/1.1\r\n\r\n") + get("/after")
            )
        );
        assertThat(output).isEqualTo(ok("POST /p") + ok("GET /after"));
    }

    @Test
    void unreadBodyNotYetReceivedIsDrained() {
        var output = withServer(
            ConnectionTest::echo,
            server -> halfClosedExchange(
                server,
                "POST /p HTTP/1.1\r\nHost: h\r\nContent-Length: 10\r\n\r\n",
                "0123456789",
                get("/after")
            )
        );
        assertThat(output).isEqualTo(ok("POST /p") + ok("GET /after"));
    }

    @Test
    void unreadBodyWithExpectContinueClosesConnection() {
        var output = withServer(
            ConnectionTest::echo,
            server -> openExchange(
                server,
                "POST /p HTTP/1.1\r\nHost: h\r\nContent-Length: 10\r\nExpect: 100-continue\r\n\r\n"
            )
        );
        assertThat(output).isEqualTo(ok("POST /p") + "<EOF>");
    }

    @Test
    void responseArrivesWhenLargeBodyIsNotRead() {
        var body = "x".repeat(100 * 1024);
        var output = withServer(
            (req, callback) -> callback.status(413).contentLength(req.path().length()).body(req.path()),
            server -> {
                try (var socket = connect(server); var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                    var out = socket.getOutputStream();
                    executor.submit(() -> {
                        try {
                            out.write(post("/big", body).getBytes(UTF_8));
                            out.flush();
                        } catch (IOException _) {
                            // Server may close before the whole body is sent
                        }
                    });
                    return readResponseBody(new BufferedInputStream(socket.getInputStream()));
                } catch (IOException e) {
                    return "<" + e + ">";
                }
            }
        );
        assertThat(output).isEqualTo("/big");
    }

    @Test
    void handlerFailureAfterResponseClosesWithoutExtraBytes() {
        var output = withServer(
            (_, callback) -> {
                callback.status(200).contentLength(3).body("abc");
                throw new IllegalStateException("Failure after response");
            },
            server -> halfClosedExchange(server, get("/one") + get("/two"))
        );
        assertThat(output).isEqualTo(response(200, "abc"));
    }

    @Test
    void handlerFailureBeforeResponseGives500AndCloses() {
        var output = withServer(
            (_, _) -> {
                throw new IllegalStateException("Failure before response");
            },
            server -> halfClosedExchange(server, get("/one") + get("/two"))
        );
        assertThat(output).isEqualTo(ERROR_500);
    }

    @Test
    void handlerFailureAfterHeadersBufferedGives500() {
        var output = withServer(
            (_, callback) -> {
                callback.status(200).header("x-partial", "yes");
                throw new IllegalStateException("Failure while writing headers");
            },
            server -> halfClosedExchange(server, get("/one"))
        );
        assertThat(output).isEqualTo(ERROR_500);
    }

    @Test
    void handlerWritingNothingGives500AndCloses() {
        var output = withServer(
            (_, _) -> {
            },
            server -> openExchange(server, get("/one"))
        );
        assertThat(output).isEqualTo(ERROR_500 + "<EOF>");
    }

    @Test
    void handlerSettingOnlyStatusGetsEmptyResponse() {
        var output = withServer(
            (_, callback) -> callback.status(401).header("x-unfinished", "yes"),
            server -> halfClosedExchange(server, get("/one") + get("/two"))
        );
        assertThat(output).isEqualTo("HTTP/1.1 401 Unauthorized\r\nx-unfinished: yes\r\ncontent-length: 0\r\n\r\n".repeat(2));
    }

    @Test
    void handlerDeclaringLengthWithoutBodyGives500AndCloses() {
        var output = withServer(
            (_, callback) -> callback.status(200).contentLength(5),
            server -> openExchange(server, get("/one"))
        );
        assertThat(output).isEqualTo(ERROR_500 + "<EOF>");
    }

    @Test
    void handlerIgnoringWriteFailureClosesConnection() {
        var output = withServer(
            (_, callback) -> {
                try {
                    callback.status(200)
                        .contentLength(5)
                        .body(Channels.newChannel(new ByteArrayInputStream("abc".getBytes(UTF_8))));
                } catch (Exception _) {
                    // Handler ignores the failure
                }
            },
            server -> openExchange(server, get("/one") + get("/two"))
        );
        assertThat(output).isEqualTo("HTTP/1.1 200 OK\r\ncontent-length: 5\r\n\r\nabc<EOF>");
    }

    @Test
    void handlerWithWrongBodyLengthGets500BeforeAnythingIsWritten() {
        var output = withServer(
            (_, callback) -> callback.status(200).contentLength(5).body("abc"),
            server -> openExchange(server, get("/one"))
        );
        assertThat(output).isEqualTo(ERROR_500 + "<EOF>");
    }

    @Test
    void justStatusIsCompleteResponse() {
        var output = withServer(
            (_, callback) -> callback.just(404),
            server -> halfClosedExchange(server, get("/one") + get("/two"))
        );
        assertThat(output).isEqualTo(response(404, "").repeat(2));
    }

    @Test
    void connectionCloseIsHonored() {
        var output = withServer(
            ConnectionTest::echo,
            server -> openExchange(
                server,
                "GET /one HTTP/1.1\r\nHost: h\r\nConnection: close\r\n\r\n" + get("/two")
            )
        );
        assertThat(output).isEqualTo(
            "HTTP/1.1 200 OK\r\ncontent-length: 8\r\nconnection: close\r\n\r\nGET /one<EOF>");
    }

    @Test
    void concurrentConnectionsGetTheirOwnResponses() {
        var connections = 32;
        var requestsPerConnection = 200;
        var failures = withServer(
            (req, callback) -> {
                var path = req.path();
                Thread.yield();
                callback.status(200).contentLength(path.length()).body(path);
            },
            server -> {
                try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                    var futures = IntStream.range(0, connections)
                        .mapToObj(connection ->
                            executor.submit(() ->
                                mismatches(server, connection, requestsPerConnection)))
                        .toList();
                    return futures.stream()
                        .map(ConnectionTest::join)
                        .flatMap(List::stream)
                        .toList();
                }
            }
        );
        assertThat(failures).isEmpty();
    }

    private static final Map<Integer, String> REASONS = Map.of(200, "OK", 404, "Not Found", 413, "Content Too Large");

    private static final String ERROR_500 =
        "HTTP/1.1 500 Internal Server Error\r\nconnection: close\r\ncontent-length: 0\r\n\r\n";

    private static void echo(HttpReq req, HttpResponseCallback callback) {
        var body = req.method() + " " + req.path();
        callback.status(200).contentLength(body.length()).body(body);
    }

    private static String get(String path) {
        return "GET " + path + " HTTP/1.1\r\nHost: h\r\n\r\n";
    }

    private static String post(String path, String body) {
        return "POST " + path + " HTTP/1.1\r\nHost: h\r\nContent-Length: " +
               body.getBytes(UTF_8).length + "\r\n\r\n" + body;
    }

    private static String ok(String body) {
        return response(200, body);
    }

    private static String response(int status, String body) {
        return "HTTP/1.1 " + status + " " + REASONS.get(status) + "\r\ncontent-length: " +
               body.getBytes(UTF_8).length + "\r\n\r\n" + body;
    }

    private static List<String> mismatches(Server.Open server, int connection, int requests) {
        try (var socket = connect(server)) {
            var out = socket.getOutputStream();
            var in = new BufferedInputStream(socket.getInputStream());
            return IntStream.range(0, requests)
                .mapToObj(request -> {
                    var path = "/c" + connection + "/r" + request;
                    write(out, get(path));
                    var body = readResponseBody(in);
                    return body.equals(path) ? null : path + " -> " + body;
                })
                .filter(Objects::nonNull)
                .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String readResponseBody(InputStream in) {
        try {
            var head = new ByteArrayOutputStream();
            while (!head.toString(UTF_8).endsWith("\r\n\r\n")) {
                var b = in.read();
                if (b < 0) {
                    return "<EOF in headers: " + head.toString(UTF_8) + ">";
                }
                head.write(b);
            }
            var length = head.toString(UTF_8).lines()
                .filter(line -> line.startsWith("content-length: "))
                .map(line -> Integer.parseInt(line.substring("content-length: ".length())))
                .findFirst()
                .orElseThrow();
            return new String(in.readNBytes(length), UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /// Writes all requests, with pauses, half-closes the connection, and reads until the server closes.
    private static String halfClosedExchange(Server.Open server, String... writes) {
        try (var socket = connect(server)) {
            var out = socket.getOutputStream();
            Arrays.stream(writes).forEach(w -> {
                write(out, w);
                pause();
            });
            socket.shutdownOutput();
            return new String(socket.getInputStream().readAllBytes(), UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /// Writes requests, with pauses, and reads until the server closes or the read times out.
    /// Stops writing if the server has closed the connection.
    private static String openExchange(Server.Open server, String... writes) {
        try (var socket = connect(server)) {
            var out = socket.getOutputStream();
            for (var w : writes) {
                try {
                    out.write(w.getBytes(UTF_8));
                    out.flush();
                } catch (IOException e) {
                    break;
                }
                pause();
            }
            var in = socket.getInputStream();
            var received = new ByteArrayOutputStream();
            var buffer = new byte[4096];
            try {
                while (true) {
                    var read = in.read(buffer);
                    if (read < 0) {
                        return received.toString(UTF_8) + "<EOF>";
                    }
                    received.write(buffer, 0, read);
                }
            } catch (SocketTimeoutException e) {
                return received.toString(UTF_8) + "<TIMEOUT>";
            } catch (SocketException e) {
                return received.toString(UTF_8) + "<EOF>";
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Socket connect(Server.Open server) throws IOException {
        var socket = new Socket();
        socket.connect(server.address());
        socket.setSoTimeout(2000);
        return socket;
    }

    private static void write(OutputStream out, String data) {
        try {
            out.write(data.getBytes(UTF_8));
            out.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void pause() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private static <T> T join(Future<T> future) {
        try {
            return future.get();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static <T> T withServer(HttpHandler handler, Function<Server.Open, T> action) {
        try (var server = Servers.create().open(new HttpCallbackProcessor(handler))) {
            return action.apply(server);
        }
    }
}
