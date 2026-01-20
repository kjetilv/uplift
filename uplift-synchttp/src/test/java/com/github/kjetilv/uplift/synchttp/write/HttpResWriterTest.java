package com.github.kjetilv.uplift.synchttp.write;

import com.github.kjetilv.uplift.synchttp.req.ResHeader;
import com.github.kjetilv.uplift.synchttp.res.HttpRes;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class HttpResWriterTest {

    @Test
    void writeSimple() {
        var baos = new ByteArrayOutputStream();
        var channel = Channels.newChannel(baos);
        new HttpResWriter(channel)
            .write(new HttpRes(
                200,
                new ResHeader("foo", "bar")
            ));
        assertThat(baos.toString(UTF_8)).isEqualTo(
            """
                HTTP/1.1 200\r
                foo: bar\r
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

            new HttpResWriter(out)
                .write(new HttpRes(
                    200,
                    13,
                    List.of(
                        new ResHeader("foo", "bar")
                    ),
                    body
                ));
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