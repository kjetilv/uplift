package com.github.kjetilv.uplift.synchttp.write;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class HttpResCallbackImplTest {

    @Test
    void writeSimple() {
        var baos = new ByteArrayOutputStream();
        var channel = Channels.newChannel(baos);
        var callback = new HttpResCallbackImpl(channel, ByteBuffer.allocate(8192));

        callback.status(200)
            .header("foo", "bar")
            .nobody();
        assertThat(baos.toString(UTF_8)).isEqualTo(
            """
               HTTP/1.1 200\r
               foo: bar\r
               content-length: 0\r
               \r
               """
        );
    }

    @Test
    void writeWithBody() throws IOException {
        ByteArrayOutputStream baos;
        try (
            var in = new ByteArrayInputStream("foobar\nzotzit".getBytes(UTF_8));
            var body = Channels.newChannel(in)
        ) {
            baos = new ByteArrayOutputStream();
            var out = Channels.newChannel(baos);

            var callback = new HttpResCallbackImpl(out, ByteBuffer.allocate(8192));
            callback.status(200)
                .header("foo", "bar")
                .contentLength(13)
                .body(body);
        }
        assertThat(baos.toString(UTF_8)).isEqualTo(
            """
                HTTP/1.1 200\r
                foo: bar\r
                content-length: 13\r
                \r
                foobar
                zotzit"""
        );
    }
}