package com.github.kjetilv.uplift.asynchttp.rere;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.lang.foreign.Arena;
import java.nio.channels.Channels;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class SyncHttpRequestParserTest {

    @Test
    void request() throws Exception {
        var headers = """
            GET /foo/bar HTTP/1.1
            foo: bar
            zipp: zoot
            Content-Type: application:json
            veryLongHeaderveryLongHeaderveryLongHeaderveryLongHeaderveryLongHeaderveryLongHeaderveryLongHeaderveryLongHeader: x
            
            sdf
            sdf
            
            
            """;

        try (var channel = Channels.newChannel(new ByteArrayInputStream(headers.getBytes()))) {
            var parser = new SyncHttpRequestParser(channel, Arena.ofShared(), 2048);
            var httpRequest = parser.parse();
            assertThat(httpRequest.requestLine()).hasToString("GET /foo/bar HTTP/1.1");
            assertThat(httpRequest.headers().getFirst()).isNotNull().satisfies(hdr ->
                assertHeader(hdr, "foo: bar"));
            assertThat(httpRequest.headers().get(1)).isNotNull().satisfies(hdr ->
                assertHeader(hdr, "zipp: zoot"));
            assertThat(httpRequest.headers().get(2)).isNotNull().satisfies(hdr ->
                assertHeader(hdr, "Content-Type: application:json"));
            assertThat(httpRequest.headers().getLast()).isNotNull().satisfies(hdr ->
                assertHeader(hdr, "veryLongHeaderveryLongHeaderveryLongHeaderveryLongHeaderveryLongHeaderveryLongHeaderveryLongHeaderveryLongHeader: x"));
            try (var linesReader = new BufferedReader(Channels.newReader(httpRequest.body(), UTF_8))) {
                assertThat(linesReader.readLine()).isEqualTo("sdf");
                assertThat(linesReader.readLine()).isEqualTo("sdf");
                assertThat(linesReader.readLine()).isBlank();
                assertThat(linesReader.readLine()).isBlank();
                assertThat(linesReader.readLine()).isNull();
            }
        }
    }

    private static void assertHeader(RequestHeader hdr, String expected) {
        assertThat(hdr.length()).isEqualTo(expected.length());
        assertThat(hdr.toString()).isEqualTo(expected);
        var name = expected.substring(0, expected.indexOf(':'));
        var value = expected.substring(expected.indexOf(':') + 2);
        assertThat(hdr.name()).isEqualTo(name);
        assertThat(hdr.value()).isEqualTo(value);
        assertThat(hdr.isContentLength()).isEqualTo(hdr.name().equalsIgnoreCase("Content-Length"));
        assertThat(hdr.separatorOffset() - hdr.offset()).isEqualTo(name.length());
    }
}