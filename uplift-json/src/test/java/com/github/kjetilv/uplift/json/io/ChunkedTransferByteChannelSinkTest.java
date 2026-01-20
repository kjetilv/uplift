package com.github.kjetilv.uplift.json.io;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class ChunkedTransferByteChannelSinkTest {

    @Test
    void test() throws IOException {
        var out = new ByteArrayOutputStream();
        var lhm = new LinkedHashMap<>();
        lhm.put("zot", "zip");
        lhm.put("foo", "bar");
        try (var channel = Channels.newChannel(out)) {
            var sink = new ChunkedTransferByteChannelSink(
                channel,
                UTF_8,
                20
            );
            JsonWrites.write(sink, lhm);
            sink.close();
        }

        assertThat(out.toString(UTF_8))
            .isEqualTo("""
                14\r
                {"zot":"zip","foo":"\r
                5\r
                bar"}\r
                0\r
                \r
                """);
    }
}