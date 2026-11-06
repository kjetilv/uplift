package com.github.kjetilv.uplift.synchttp.write;

import module java.base;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class ResponseHeaderTest {

    @Test
    void nonAsciiHeaderValueIsUtf8() {
        assertThat(respond(callback -> callback.status(200).header("x-name", "blåbær").nobody()))
            .isEqualTo("HTTP/1.1 200 OK\r\nx-name: blåbær\r\ncontent-length: 0\r\n\r\n");
    }

    @Test
    void contentLengthDigits() {
        for (var length : List.of(1L, 9L, 10L, 99L, 100L, 12345L, 1_000_000L)) {
            var body = new byte[Math.toIntExact(length)];
            assertThat(respond(callback -> callback.status(200).contentLength(length).body(body)))
                .startsWith("HTTP/1.1 200 OK\r\ncontent-length: " + length + "\r\n\r\n")
                .hasSize(("HTTP/1.1 200 OK\r\ncontent-length: " + length + "\r\n\r\n").length() + body.length);
        }
    }

    @Test
    void bodyLargerThanBuffer() {
        var body = "y".repeat(10_000).getBytes(UTF_8);
        assertThat(respond(callback -> callback.status(200).contentLength(body.length).body(body)))
            .isEqualTo("HTTP/1.1 200 OK\r\ncontent-length: 10000\r\n\r\n" + "y".repeat(10_000));
    }

    @Test
    void bodyThatFillsBufferExactly() {
        var head = "HTTP/1.1 200 OK\r\ncontent-length: 4055\r\n\r\n";
        var body = "z".repeat(4096 - head.length()).getBytes(UTF_8);
        assertThat(respond(callback -> callback.status(200).contentLength(body.length).body(body)))
            .isEqualTo(head + "z".repeat(body.length));
    }

    private static String respond(Consumer<HttpResponseCallback> response) {
        var out = new ByteArrayOutputStream();
        response.accept(HttpResponseCallback.create(Channels.newChannel(out), ByteBuffer.allocate(4096)));
        return out.toString(UTF_8);
    }
}
