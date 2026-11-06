package com.github.kjetilv.uplift.synchttp;

import module java.base;
import com.github.kjetilv.uplift.synchttp.read.HttpReqException;
import com.github.kjetilv.uplift.synchttp.read.HttpReqReader;
import com.github.kjetilv.uplift.synchttp.rere.HttpReq;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/// Tests reading requests that arrive in several reads, and several requests from one channel.
class HttpReqReaderStreamTest {

    @Test
    void requestSplitAtEveryPosition() {
        var request = "GET /a?x=1 HTTP/1.1\r\nHost: example\r\nAccept: text/plain\r\nX-Long: " +
                      "y".repeat(100) + "\r\n\r\n";
        IntStream.range(1, request.length()).forEach(split -> {
            var req = HttpReqReader.defaultReader()
                .read(chunks(request.substring(0, split), request.substring(split)));
            assertThat(describe(req))
                .describedAs("split at %d", split)
                .isEqualTo("GET /a?x=1 HTTP/1.1 [Host: example, Accept: text/plain, X-Long: " + "y".repeat(100) + "]");
        });
    }

    @Test
    void requestOneByteAtATime() {
        var request = "GET /a HTTP/1.1\r\nHost: example\r\nAccept: text/plain\r\n\r\n";
        var req = HttpReqReader.defaultReader()
            .read(chunks(request.chars().mapToObj(c -> String.valueOf((char) c)).toArray(String[]::new)));
        assertThat(describe(req)).isEqualTo("GET /a HTTP/1.1 [Host: example, Accept: text/plain]");
    }

    @Test
    void severalRequestsFromOneRead() {
        var reader = HttpReqReader.defaultReader();
        var channel = chunks(
            "GET /one HTTP/1.1\r\nA: 1\r\n\r\n" +
            "POST /two HTTP/1.1\r\nContent-Length: 5\r\n\r\nhello" +
            "GET /three HTTP/1.1\r\nB: 3\r\n\r\n"
        );
        assertThat(describe(reader.read(channel))).isEqualTo("GET /one HTTP/1.1 [A: 1]");
        var post = reader.read(channel);
        assertThat(describe(post)).isEqualTo("POST /two HTTP/1.1 [Content-Length: 5]");
        assertThat(post.bodyString()).isEqualTo("hello");
        assertThat(describe(reader.read(channel))).isEqualTo("GET /three HTTP/1.1 [B: 3]");
        assertThat(reader.read(channel)).isNull();
    }

    @Test
    void unreadBufferedBodyIsSkipped() {
        var reader = HttpReqReader.defaultReader();
        var channel = chunks(
            "POST /one HTTP/1.1\r\nContent-Length: 18\r\n\r\nGET /x HTTP/1.1\r\n\r\n" +
            "GET /two HTTP/1.1\r\n\r\n"
        );
        assertThat(describe(reader.read(channel))).isEqualTo("POST /one HTTP/1.1 [Content-Length: 18]");
        assertThat(describe(reader.read(channel))).isEqualTo("GET /two HTTP/1.1 []");
    }

    @Test
    void bodyIsReadAcrossBufferAndChannel() {
        var reader = HttpReqReader.defaultReader();
        var channel = chunks(
            "POST /one HTTP/1.1\r\nContent-Length: 10\r\n\r\n0123",
            "456789GET /two HTTP/1.1\r\n\r\n"
        );
        var post = reader.read(channel);
        assertThat(post.bodyString()).isEqualTo("0123456789");
        assertThat(describe(reader.read(channel))).isEqualTo("GET /two HTTP/1.1 []");
    }

    @Test
    void bodyChannelStopsAtContentLength() throws IOException {
        var reader = HttpReqReader.defaultReader();
        var channel = chunks(
            "POST /one HTTP/1.1\r\nContent-Length: 4\r\n\r\nab",
            "cdGET /two HTTP/1.1\r\n\r\n"
        );
        var post = reader.read(channel);
        assertThat(new String(post.bodyStream().readAllBytes(), UTF_8)).isEqualTo("abcd");
        assertThat(describe(reader.read(channel))).isEqualTo("GET /two HTTP/1.1 []");
    }

    @Test
    void requestWithoutContentLengthHasEmptyBody() throws IOException {
        var reader = HttpReqReader.defaultReader();
        var channel = chunks("GET /one HTTP/1.1\r\n\r\nGET /two HTTP/1.1\r\n\r\n");
        var first = reader.read(channel);
        assertThat(first.bodyStream().readAllBytes()).isEmpty();
        assertThat(describe(reader.read(channel))).isEqualTo("GET /two HTTP/1.1 []");
    }

    @Test
    void emptyLinesBeforeRequestAreSkipped() {
        var reader = HttpReqReader.defaultReader();
        var channel = chunks("GET /one HTTP/1.1\r\n\r\n\r\n\nGET /two HTTP/1.1\r\n\r\n");
        assertThat(describe(reader.read(channel))).isEqualTo("GET /one HTTP/1.1 []");
        assertThat(describe(reader.read(channel))).isEqualTo("GET /two HTTP/1.1 []");
    }

    @Test
    void closedChannelGivesNull() {
        assertThat(HttpReqReader.defaultReader().read(chunks())).isNull();
    }

    @Test
    void malformedRequestLineFails() {
        assertStatus(() -> HttpReqReader.defaultReader().read(chunks("GARBAGE\r\nA: 1\r\n\r\n")), 400);
    }

    @Test
    void headerLineWithoutColonFails() {
        assertStatus(() -> HttpReqReader.defaultReader().read(chunks("GET / HTTP/1.1\r\nNoColon\r\nA: b\r\n\r\n")), 400);
    }

    @Test
    void tooLargeHeaderSectionFails() {
        var request = "GET / HTTP/1.1\r\nCookie: " + "x".repeat(5000) + "\r\n\r\n";
        assertStatus(() -> HttpReqReader.defaultReader().read(chunks(request, "")), 431);
    }

    static void assertStatus(ThrowingCallable callable, int status) {
        assertThatThrownBy(callable)
            .isInstanceOfSatisfying(HttpReqException.class, e -> assertThat(e.status()).isEqualTo(status));
    }

    private static String describe(HttpReq req) {
        return req.reqLine() + " " + req.headers().stream().map(Object::toString).toList();
    }

    /// A channel that returns one chunk per read, then end of stream.
    /// A chunk larger than the buffer is returned over several reads.
    private static ReadableByteChannel chunks(String... chunks) {
        var queue = Arrays.stream(chunks)
            .map(chunk -> ByteBuffer.wrap(chunk.getBytes(UTF_8)))
            .collect(Collectors.toCollection(ArrayDeque::new));
        return new ReadableByteChannel() {

            @Override
            public int read(ByteBuffer dst) {
                while (!queue.isEmpty() && !queue.peek().hasRemaining()) {
                    queue.poll();
                }
                var next = queue.peek();
                if (next == null) {
                    return -1;
                }
                var count = Math.min(dst.remaining(), next.remaining());
                dst.put(next.slice(next.position(), count));
                next.position(next.position() + count);
                return count;
            }

            @Override
            public boolean isOpen() {
                return true;
            }

            @Override
            public void close() {
            }
        };
    }
}
