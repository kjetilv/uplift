package com.github.kjetilv.uplift.asynchttp.rere;

import com.github.kjetilv.uplift.asynchttp.rere.RequestLine.Method;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.lang.foreign.Arena;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class SyncHttpRequestReaderTest {

    @Test
    void shortRequest() {
        assertSelf(SHORT_REQ);
    }

    @Test
    void shortRequestNoBody() throws Exception {
        try (var channel = channel(SHORT_REQ)) {
            var request = parse(channel);
            try (var reader = reader(request)) {
                assertThat(reader.readLine()).isNull();
            }
        }
    }

    @Test
    void shortRequestWLineBreaks() {
        assertSelf(HTTP_LINE_BREAKS);
    }

    @Test
    void shortRequestWLineBreaksNoBdy() throws Exception {
        try (var channel = channel(HTTP_LINE_BREAKS)) {
            var request = parse(channel);
            try (var reader = reader(request)) {
                assertThat(reader.readLine()).isNull();
            }
        }
    }

    @Test
    void shortRequestWLineBreaksNoBody() throws Exception {
        try (var channel = channel(HTTP_LINE_BREAKS)) {
            var request = parse(channel);
            try (var reader = reader(request)) {
                assertThat(reader.readLine()).isNull();
            }
        }
    }

    @Test
    void contentLength() throws Exception {
        try (
            var channel = channel(GET_CONTENT_LEN)
        ) {
            var request = parse(channel);
            assertThat(request.headers()).singleElement().satisfies(header ->
                assertThat(header.isContentLength()).isTrue());
            assertThat(request.contentLength()).isEqualTo(10);
        }
    }

    @Test
    void shortRequestW2LineBreaks() {
        assertSelf(HTTP_2_LINE_BREAKS);
    }

    @Test
    void shortRequestW2LineBreaksBody() throws Exception {
        try (var channel = channel(HTTP_2_LINE_BREAKS)) {
            var request = parse(channel);
            try (var reader = reader(request)) {
                assertThat(reader.readLine()).isEqualTo("");
                assertThat(reader.readLine()).isNull();
            }
        }
    }

    @Test
    void requestWithBody() throws Exception {
        try (var channel = channel(LONG_REQ)) {
            var httpRequest = parse(channel);
            assertThat(httpRequest.method()).isSameAs(Method.GET);
            assertThat(httpRequest.requestLine()).hasToString("GET /foo/bar HTTP/1.1");
            assertThat(httpRequest.headers()[0]).isNotNull().satisfies(hdr ->
                assertHeader(hdr, "foo: bar"));
            assertThat(httpRequest.headers()[1]).isNotNull().satisfies(hdr ->
                assertHeader(hdr, "zipp: zoot"));
            assertThat(httpRequest.headers()[2]).isNotNull().satisfies(hdr ->
                assertHeader(hdr, "Content-Type: application:json"));
            assertThat(httpRequest.headers()[3]).isNotNull().satisfies(hdr ->
                assertHeader(
                    hdr,
                    "veryLongHeaderveryLongHeaderveryLongHeaderveryLongHeaderveryLongHeaderveryLongHeaderveryLongHeaderveryLongHeader: x"
                ));
            try (var linesReader = reader(httpRequest)) {
                assertThat(linesReader.readLine()).isEqualTo("sdf");
                assertThat(linesReader.readLine()).isEqualTo("sdf");
                assertThat(linesReader.readLine()).isBlank();
                assertThat(linesReader.readLine()).isBlank();
                assertThat(linesReader.readLine()).isNull();
            }
        }
    }

    private static final String HTTP_2_LINE_BREAKS =
        """
            GET foo/bar HTTP/1.1
            foo: bar
            
            
            """;

    private static final String LONG_REQ =
        """
            GET /foo/bar HTTP/1.1
            foo: bar
            zipp: zoot
            Content-Type: application:json
            veryLongHeaderveryLongHeaderveryLongHeaderveryLongHeaderveryLongHeaderveryLongHeaderveryLongHeaderveryLongHeader: x
            
            sdf
            sdf
            
            
            """;

    private static final String SHORT_REQ =
        """
            GET foo/bar HTTP/1.1
            foo: bar
            """;

    private static final String HTTP_LINE_BREAKS = """
        GET foo/bar HTTP/1.1
        foo: bar
        
        """;

    public static final String GET_CONTENT_LEN = """
        GET foo/bar HTTP/1.1
        Content-Length: 10
        
        """;

    private static ReadableByteChannel channel(String str) {
        return Channels.newChannel(new ByteArrayInputStream(str.getBytes()));
    }

    private static HttpRequest parse(ReadableByteChannel channel) {
        return new SyncHttpRequestReader(channel, Arena.ofShared(), 2048).parse();
    }

    private static BufferedReader reader(HttpRequest httpRequest) {
        return new BufferedReader(Channels.newReader(httpRequest.body(), UTF_8));
    }

    private static void assertSelf(String req) {
        try (var channel = channel(req)) {
            var request = new SyncHttpRequestReader(channel, Arena.ofAuto(), 2048).parse();
            assertThat(request).hasToString(req.trim() + "\n");
        } catch (Exception e) {
            throw new IllegalStateException(e);
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