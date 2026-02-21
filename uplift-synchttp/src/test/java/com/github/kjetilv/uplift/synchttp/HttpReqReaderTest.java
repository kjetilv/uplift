package com.github.kjetilv.uplift.synchttp;

import module java.base;
import com.github.kjetilv.uplift.synchttp.read.HttpReqReader;
import com.github.kjetilv.uplift.synchttp.rere.HttpReq;
import com.github.kjetilv.uplift.synchttp.rere.ReqHeader;
import org.junit.jupiter.api.Test;

import static com.github.kjetilv.uplift.synchttp.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class HttpReqReaderTest {

    @Test
    void rootRequest() {
        assertSelf(ROOT);
    }

    @Test
    void shortRequest() {
        assertSelf(SHORT_REQ);
    }

    @Test
    void shortRequestTrimmed() {
        assertSelf(SHORT_REQ.trim());
    }

    @Test
    void shortRequestNoBody() throws Exception {
        try (
            var channel = channel(SHORT_REQ);
            var request = parse(channel);
            var reader = reader(request)
        ) {
            assertThat(reader.readLine()).isNull();
        }
    }

    @Test
    void shortRequestTrimmedNoBody() throws Exception {
        try (
            var channel = channel(SHORT_REQ.trim());
            var request = parse(channel);
            var reader = reader(request)
        ) {
            assertThat(reader.readLine()).isNull();
        }
    }

    @Test
    void shortRequestWLineBreaks() {
        assertSelf(HTTP_LINE_BREAKS);
    }

    @Test
    void shortRequestWLineBreaksNoBdy() throws Exception {
        try (
            var channel = channel(HTTP_LINE_BREAKS);
            var request = parse(channel);
            var reader = reader(request)
        ) {
            assertThat(request.method()).isSameAs(GET);
            assertThat(reader.readLine()).isNull();
        }
    }

    @Test
    void shortRequestWLineBreaksNoBody() throws Exception {
        try (
            var channel = channel(HTTP_LINE_BREAKS);
            var request = parse(channel);
            var reader = reader(request)
        ) {
            assertThat(reader.readLine()).isNull();
        }
    }

    @Test
    void contentLength() throws Exception {
        try (
            var channel = channel(GET_CONTENT_LEN);
            var request = parse(channel);
        ) {
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
            assertThat(httpRequest.method()).isSameAs(GET);
            assertThat(httpRequest.reqLine()).hasToString("GET /foo/bar HTTP/1.1");
            assertThat(httpRequest.header(0)).isNotNull().satisfies(hdr ->
                assertHeader(hdr, "foo: bar"));
            assertThat(httpRequest.header(1)).isNotNull().satisfies(hdr ->
                assertHeader(hdr, "zipp: zoot"));
            assertThat(httpRequest.header(2)).isNotNull().satisfies(hdr ->
                assertHeader(hdr, "Content-Type: application:json"));
            assertThat(httpRequest.header(3)).isNotNull().satisfies(hdr ->
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

    @Test
    void qp() throws Exception {
        try (var channel = channel(HTTP_LINE_BREAKS_QP)) {
            var httpRequest = parse(channel);
            var withQp = httpRequest.withQueryParameters();

            var queryParameters = withQp.reqLine().queryParameters().parameters();
            assertThat(queryParameters[0].name()).isEqualTo("president");
            assertThat(queryParameters[0].value()).isEqualTo("crazy");
            assertThat(queryParameters[1].name()).isEqualTo("congress");
            assertThat(queryParameters[1].value()).isEqualTo("supine");
        }
    }

    @Test
    void qp2() throws Exception {
        try (var channel = channel(HTTP_LINE_BREAKS_QP_2)) {
            var req = parse(channel);
            assertThat(req.reqLine().urlLength()).isEqualTo(49);
            var withQp = req.withQueryParameters();
            assertThat(withQp.reqLine().urlLength()).isEqualTo(7);

            assertThat(req.path()).startsWith(withQp.path() + "?");
            assertThat(withQp.path()).isEqualTo("foo/bar");

            var qps = withQp.reqLine().queryParameters().parameters();
            assertThat(qps[0].name()).isEqualTo("president");
            assertThat(qps[0].value()).isEqualTo("crazy");
            assertThat(qps[1].name()).isEqualTo("foo");
            assertThat(qps[1].value()).isEqualTo("");
            assertThat(qps[2].name()).isEqualTo("bar");
            assertThat(qps[2].value()).isEqualTo("");
            assertThat(qps[3].name()).isEqualTo("congress");
            assertThat(qps[3].value()).isEqualTo("supine");
        }
    }

    private static final String GET_CONTENT_LEN = """
        GET foo/bar HTTP/1.1\r
        Content-Length: 10\r
        \r
        """;

    private static final String HTTP_2_LINE_BREAKS = """
        GET foo/bar HTTP/1.1\r
        foo: bar\r
        \r
        
        """;

    private static final String LONG_REQ = """
        GET /foo/bar HTTP/1.1\r
        foo: bar\r
        zipp: zoot\r
        Content-Type: application:json\r
        veryLongHeaderveryLongHeaderveryLongHeaderveryLongHeaderveryLongHeaderveryLongHeaderveryLongHeaderveryLongHeader: x\r
        \r
        sdf
        sdf
        
        
        """;

    private static final String ROOT = """
        GET / HTTP/1.1\r
        foo: bar\r
        """;

    private static final String SHORT_REQ = """
        GET foo/bar HTTP/1.1\r
        foo: bar\r
        """;

    private static final String HTTP_LINE_BREAKS = """
        GET foo/bar HTTP/1.1\r
        foo: bar\r
        \r
        """;

    private static final String HTTP_LINE_BREAKS_QP = """
        GET foo/bar?president=crazy&congress=supine HTTP/1.1\r
        foo: bar\r
        \r
        """;

    private static final String HTTP_LINE_BREAKS_QP_2 = """
        GET foo/bar?president=crazy&foo=&bar=&congress=supine HTTP/1.1\r
        foo: bar\r
        \r
        """;

    private static ReadableByteChannel channel(String str) {
        return Channels.newChannel(new ByteArrayInputStream(str.getBytes()));
    }

    private static HttpReq parse(ReadableByteChannel channel) {
        return HttpReqReader.defaultReader().read(channel);
    }

    private static BufferedReader reader(HttpReq httpReq) {
        var body = httpReq.body();
        return new BufferedReader(Channels.newReader(body, UTF_8));
    }

    private static void assertSelf(String req) {
        try (var channel = channel(req)) {
            var request = HttpReqReader.defaultReader().read(channel);
            assertThat(request).hasToString(req.trim() + "\r\n");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static void assertHeader(ReqHeader hdr, String expected) {
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