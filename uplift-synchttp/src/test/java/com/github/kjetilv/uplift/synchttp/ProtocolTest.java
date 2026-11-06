package com.github.kjetilv.uplift.synchttp;

import module java.base;
import com.github.kjetilv.uplift.synchttp.rere.HttpReq;
import com.github.kjetilv.uplift.synchttp.write.HttpResponseCallback;
import org.junit.jupiter.api.Test;

import static com.github.kjetilv.uplift.synchttp.TestConnections.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/// Tests for protocol handling: errors, limits, timeouts, versions, chunked bodies and 100 Continue.
class ProtocolTest {

    @Test
    void malformedRequestGets400() {
        assertThat(withServer(ProtocolTest::echo, server -> openExchange(server, "GARBAGE\r\n\r\n")))
            .isEqualTo(error(400, "Bad Request"));
    }

    @Test
    void headerWithoutNameGets400() {
        assertThat(withServer(ProtocolTest::echo, server -> openExchange(server, "GET / HTTP/1.1\r\n: x\r\n\r\n")))
            .isEqualTo(error(400, "Bad Request"));
    }

    @Test
    void unknownMethodGets501() {
        assertThat(withServer(ProtocolTest::echo, server -> openExchange(server, "BREW / HTTP/1.1\r\n\r\n")))
            .isEqualTo(error(501, "Not Implemented"));
    }

    @Test
    void patchIsSupported() {
        assertThat(withServer(ProtocolTest::echo, server -> halfClosedExchange(server, "PATCH /p HTTP/1.1\r\n\r\n")))
            .isEqualTo(ok("PATCH /p "));
    }

    @Test
    void unsupportedVersionGets505() {
        assertThat(withServer(ProtocolTest::echo, server -> openExchange(server, "GET / HTTP/2.0\r\n\r\n")))
            .isEqualTo(error(505, "HTTP Version Not Supported"));
    }

    @Test
    void invalidContentLengthGets400() {
        for (var value : List.of("abc", "+10", "-1", "1 0", "99999999999999999999")) {
            assertThat(withServer(
                ProtocolTest::echo,
                server -> openExchange(server, "POST / HTTP/1.1\r\nContent-Length: " + value + "\r\n\r\n")
            )).describedAs(value).isEqualTo(error(400, "Bad Request"));
        }
    }

    @Test
    void differentContentLengthsGet400() {
        assertThat(withServer(
            ProtocolTest::echo,
            server -> openExchange(server, "POST / HTTP/1.1\r\nContent-Length: 3\r\nContent-Length: 4\r\n\r\nabcd")
        )).isEqualTo(error(400, "Bad Request"));
    }

    @Test
    void equalContentLengthsAreAccepted() {
        assertThat(withServer(
            ProtocolTest::echo,
            server -> halfClosedExchange(server, "POST / HTTP/1.1\r\nContent-Length: 3\r\nContent-Length: 3\r\n\r\nabc")
        )).isEqualTo(ok("POST / abc"));
    }

    @Test
    void tooLargeHeadersGet431() {
        var request = "GET / HTTP/1.1\r\nCookie: " + "x".repeat(5000) + "\r\n\r\n";
        assertThat(withServer(ProtocolTest::echo, server -> openExchange(server, request)))
            .isEqualTo(error(431, "Request Header Fields Too Large"));
    }

    @Test
    void largeResponseHeadersAreSent() {
        var value = "v".repeat(6000);
        var output = withServer(
            (_, callback) -> callback.status(200).header("x-large", value).contentLength(2).body("ok"),
            server -> halfClosedExchange(server, get("/"))
        );
        assertThat(output).isEqualTo("HTTP/1.1 200 OK\r\nx-large: " + value + "\r\ncontent-length: 2\r\n\r\nok");
    }

    @Test
    void failureAfterLargeResponseHeadersAddsNoErrorResponse() {
        var value = "v".repeat(6000);
        var output = withServer(
            (_, callback) -> {
                callback.status(200).header("x-large", value).header("x-more", "m");
                throw new IllegalStateException("Failure after headers were flushed");
            },
            server -> openExchange(server, get("/"))
        );
        assertThat(output)
            .startsWith("HTTP/1.1 200 OK\r\nx-large: " + value)
            .endsWith("<EOF>")
            .doesNotContain("500");
    }

    @Test
    void idleConnectionIsClosedAfterReadTimeout() {
        var output = withServer(
            Duration.ofMillis(300),
            ProtocolTest::echo,
            server -> {
                try (var socket = connect(server)) {
                    socket.getOutputStream().write(get("/one").getBytes(UTF_8));
                    var start = System.nanoTime();
                    var received = readUntilClosed(socket);
                    return received + " " + (Duration.ofNanos(System.nanoTime() - start).toMillis() < 2500);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        );
        assertThat(output).isEqualTo(ok("GET /one ") + "<EOF> true");
    }

    @Test
    void http10ClosesByDefault() {
        assertThat(withServer(ProtocolTest::echo, server -> openExchange(server, "GET /a HTTP/1.0\r\n\r\n")))
            .isEqualTo(okClosing("GET /a ") + "<EOF>");
    }

    @Test
    void http10KeepAlive() {
        var request = "GET /a HTTP/1.0\r\nConnection: keep-alive\r\n\r\n";
        assertThat(withServer(ProtocolTest::echo, server -> halfClosedExchange(server, request + request)))
            .isEqualTo(ok("GET /a ").repeat(2));
    }

    @Test
    void handlerConnectionHeaderIsNotDuplicated() {
        var output = withServer(
            (_, callback) -> callback.status(200).headers("connection: keep-alive").contentLength(2).body("ok"),
            server -> openExchange(server, "GET / HTTP/1.1\r\nConnection: close\r\n\r\n")
        );
        assertThat(output).isEqualTo("HTTP/1.1 200 OK\r\nconnection: keep-alive\r\ncontent-length: 2\r\n\r\nok<EOF>");
    }

    @Test
    void largeBodyThroughGatheringWrite() {
        var body = "w".repeat(50_000);
        var output = withServer(
            (_, callback) -> callback.status(200).contentLength(body.length()).body(body),
            server -> halfClosedExchange(server, get("/"))
        );
        assertThat(output).isEqualTo(ok(body));
    }

    @Test
    void chunkedBody() {
        var request = "POST /c HTTP/1.1\r\nTransfer-Encoding: chunked\r\n\r\n" +
                      "5\r\nhello\r\n6;ext=1\r\n world\r\n0\r\nTrailer: t\r\n\r\n";
        assertThat(withServer(ProtocolTest::echo, server -> halfClosedExchange(server, request + get("/after"))))
            .isEqualTo(ok("POST /c hello world") + ok("GET /after "));
    }

    @Test
    void unreadChunkedBodyIsDrained() {
        var request = "POST /c HTTP/1.1\r\nTransfer-Encoding: chunked\r\n\r\n5\r\nhello\r\n0\r\n\r\n";
        assertThat(withServer(ProtocolTest::path, server -> halfClosedExchange(server, request, get("/after"))))
            .isEqualTo(ok("/c") + ok("/after"));
    }

    @Test
    void invalidChunkSizeGets400() {
        var request = "POST /c HTTP/1.1\r\nTransfer-Encoding: chunked\r\n\r\nzz\r\nhello\r\n0\r\n\r\n";
        assertThat(withServer(ProtocolTest::echo, server -> openExchange(server, request)))
            .isEqualTo(error(400, "Bad Request"));
    }

    @Test
    void transferEncodingWithContentLengthGets400() {
        var request = "POST /c HTTP/1.1\r\nTransfer-Encoding: chunked\r\nContent-Length: 5\r\n\r\n0\r\n\r\n";
        assertThat(withServer(ProtocolTest::echo, server -> openExchange(server, request)))
            .isEqualTo(error(400, "Bad Request"));
    }

    @Test
    void severalTransferEncodingHeadersGet400() {
        var request = "POST /c HTTP/1.1\r\nTransfer-Encoding: chunked\r\nTransfer-Encoding: chunked\r\n\r\n0\r\n\r\n";
        assertThat(withServer(ProtocolTest::echo, server -> openExchange(server, request)))
            .isEqualTo(error(400, "Bad Request"));
    }

    @Test
    void unsupportedTransferEncodingGets501() {
        var request = "POST /c HTTP/1.1\r\nTransfer-Encoding: gzip, chunked\r\n\r\n0\r\n\r\n";
        assertThat(withServer(ProtocolTest::echo, server -> openExchange(server, request)))
            .isEqualTo(error(501, "Not Implemented"));
    }

    @Test
    void expectContinue() {
        var output = withServer(
            ProtocolTest::echo,
            server -> {
                try (var socket = connect(server)) {
                    var out = socket.getOutputStream();
                    out.write("POST /e HTTP/1.1\r\nContent-Length: 5\r\nExpect: 100-continue\r\n\r\n".getBytes(UTF_8));
                    out.flush();
                    var interim = readHead(socket.getInputStream());
                    out.write("hello".getBytes(UTF_8));
                    out.flush();
                    socket.shutdownOutput();
                    return interim + readUntilClosed(socket);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        );
        assertThat(output).isEqualTo("HTTP/1.1 100 Continue\r\n\r\n" + ok("POST /e hello") + "<EOF>");
    }

    @Test
    void expectContinueWithBodyNotReadSendsNoContinue() {
        var output = withServer(
            ProtocolTest::path,
            server -> openExchange(server, "POST /e HTTP/1.1\r\nContent-Length: 5\r\nExpect: 100-continue\r\n\r\n")
        );
        assertThat(output).isEqualTo(ok("/e") + "<EOF>");
    }

    @Test
    void queryParametersAreDecoded() {
        var output = withServer(
            (req, callback) -> {
                var params = req.queryParameters();
                var body = params.par("q") + "|" + params.par("name") + "|" + params.par("bad");
                callback.status(200).contentLength(body.getBytes(UTF_8).length).body(body);
            },
            server -> halfClosedExchange(server, get("/s?q=a%20b+c&n%61me=bl%C3%A5&bad=%zz%2"))
        );
        assertThat(output).isEqualTo(ok("a b c|blå|%zz%2"));
    }

    private static void echo(HttpReq req, HttpResponseCallback callback) {
        var body = req.method() + " " + req.path() + " " + req.bodyString();
        callback.status(200).contentLength(body.getBytes(UTF_8).length).body(body);
    }

    private static void path(HttpReq req, HttpResponseCallback callback) {
        var body = req.path();
        callback.status(200).contentLength(body.length()).body(body);
    }

    private static String ok(String body) {
        return "HTTP/1.1 200 OK\r\ncontent-length: " + body.getBytes(UTF_8).length + "\r\n\r\n" + body;
    }

    private static String okClosing(String body) {
        return "HTTP/1.1 200 OK\r\ncontent-length: " + body.getBytes(UTF_8).length +
               "\r\nconnection: close\r\n\r\n" + body;
    }

    private static String error(int status, String reason) {
        return "HTTP/1.1 " + status + " " + reason + "\r\nconnection: close\r\ncontent-length: 0\r\n\r\n<EOF>";
    }
}
