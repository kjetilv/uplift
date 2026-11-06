package com.github.kjetilv.uplift.synchttp;

import module java.base;
import com.github.kjetilv.uplift.synchttp.read.HttpReqReader;
import com.github.kjetilv.uplift.synchttp.rere.HttpReq;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/// Tests for query parameters and header values.
class RequestParsingTest {

    @Test
    void queryParameters() {
        assertThat(query("GET /a?x=1&y=2 HTTP/1.1\r\nHost: h\r\n\r\n")).isEqualTo("/a [x=1, y=2]");
    }

    @Test
    void noQueryParameters() {
        assertThat(query("GET /a HTTP/1.1\r\nHost: h\r\n\r\n")).isEqualTo("/a []");
    }

    @Test
    void queryStopsAtEndOfUrl() {
        assertThat(query("GET /a?x=1 HTTP/1.1\r\nCookie: k=v\r\n\r\n")).isEqualTo("/a [x=1]");
        assertThat(query("GET /a?x=1 HTTP/1.1\r\nX: a&b=c\r\n\r\n")).isEqualTo("/a [x=1]");
    }

    @Test
    void questionMarkAfterUrlIsIgnored() {
        assertThat(query("GET / HTTP/1.1\r\nX:?a=b\r\n\r\n")).isEqualTo("/ []");
        assertThat(query("GET /a HTTP/1.1\r\nReferer: /b?c=d\r\n\r\n")).isEqualTo("/a []");
    }

    @Test
    void parameterWithoutValue() {
        assertThat(query("GET /a?flag HTTP/1.1\r\n\r\n")).isEqualTo("/a [flag=]");
        assertThat(query("GET /a?a=1&b HTTP/1.1\r\n\r\n")).isEqualTo("/a [a=1, b=]");
    }

    @Test
    void emptyQuery() {
        assertThat(query("GET /a? HTTP/1.1\r\n\r\n")).isEqualTo("/a []");
    }

    @Test
    void emptyParametersAreSkipped() {
        assertThat(query("GET /a?&&a=1&&b=2& HTTP/1.1\r\n\r\n")).isEqualTo("/a [a=1, b=2]");
    }

    @Test
    void valueMayContainEquals() {
        assertThat(query("GET /a?a=1=2 HTTP/1.1\r\n\r\n")).isEqualTo("/a [a=1=2]");
    }

    @Test
    void parameterLookup() {
        var params = read("GET /a?a=1&b=&c HTTP/1.1\r\n\r\n").queryParameters();
        assertThat(params.par("a")).isEqualTo("1");
        assertThat(params.par("b")).isEmpty();
        assertThat(params.par("c")).isEmpty();
        assertThat(params.par("d")).isNull();
    }

    @Test
    void duplicateQueryParametersAreJoined() {
        assertThat(read("GET /a?a=1&b=x&a=2 HTTP/1.1\r\n\r\n").queryParametersMap())
            .isEqualTo(Map.of("a", "1,2", "b", "x"));
    }

    @Test
    void duplicateHeadersAreJoined() {
        assertThat(read("GET / HTTP/1.1\r\nA: 1\r\nB: x\r\nA: 2\r\n\r\n").headerMap())
            .isEqualTo(Map.of("A", "1,2", "B", "x"));
    }

    @Test
    void headerLookupIgnoresCase() {
        var req = read("GET / HTTP/1.1\r\nX-Custom-Header: v\r\nContent-Length: 0\r\n\r\n");
        assertThat(req.headers().header("x-custom-header")).isEqualTo("v");
        assertThat(req.headers().header("X-CUSTOM-HEADER")).isEqualTo("v");
        assertThat(req.headers().header("X-Custom-Header")).isEqualTo("v");
        assertThat(req.headers().header("x-custom-headex")).isNull();
        assertThat(req.headers().header("x-custom")).isNull();
        assertThat(req.headers().header("blå")).isNull();
        assertThat(req.headers().header(0).isContentLength()).isFalse();
        assertThat(req.headers().header(1).isContentLength()).isTrue();
    }

    @Test
    void headerValueWithoutSpace() {
        assertThat(read("GET / HTTP/1.1\r\nHost:example\r\n\r\n").host()).isEqualTo("example");
    }

    @Test
    void headerValueIsTrimmed() {
        assertThat(read("GET / HTTP/1.1\r\nHost: \t example \t \r\n\r\n").host()).isEqualTo("example");
    }

    @Test
    void emptyHeaderValue() {
        assertThat(read("GET / HTTP/1.1\r\nHost:\r\nX: \r\n\r\n").headerMap())
            .isEqualTo(Map.of("Host", "", "X", ""));
    }

    @Test
    void indexOfStaysInRange() {
        var segment = MemorySegment.ofArray("abc?defghijklmnopqrstuvwxyz0123456789?".getBytes(UTF_8));
        assertThat(Utils.indexOf((byte) '?', segment, 0, 3)).isEqualTo(-1);
        assertThat(Utils.indexOf((byte) '?', segment, 0, 4)).isEqualTo(3);
        assertThat(Utils.indexOf((byte) '?', segment, 4, 33)).isEqualTo(-1);
        assertThat(Utils.indexOf((byte) '?', segment, 4, 34)).isEqualTo(37);
    }

    @Test
    void indexOfAtEndOfSegment() {
        var segment = MemorySegment.ofArray("ab?".getBytes(UTF_8));
        assertThat(Utils.indexOf((byte) '?', segment, 0, 3)).isEqualTo(2);
        assertThat(Utils.indexOf((byte) 'x', segment, 0, 3)).isEqualTo(-1);
    }

    private static String query(String request) {
        var req = read(request).withQueryParameters();
        return req.path() + " " + Arrays.toString(req.reqLine().queryParameters().parameters());
    }

    private static HttpReq read(String request) {
        return HttpReqReader.defaultReader()
            .read(Channels.newChannel(new ByteArrayInputStream(request.getBytes(UTF_8))));
    }
}
