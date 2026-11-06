package com.github.kjetilv.uplift.synchttp.read;

import module java.base;
import com.github.kjetilv.uplift.synchttp.rere.HttpReq;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChunkedBodyTest {

    private static final String REQUEST =
        "POST /c HTTP/1.1\r\nTransfer-Encoding: chunked\r\n\r\n" +
        "5\r\nhello\r\nA;name=value\r\n, chunked!\r\n0\r\nX-Trailer: yes\r\n\r\n" +
        "GET /next HTTP/1.1\r\nA: 1\r\n\r\n";

    @Test
    void decodesAndKeepsNextRequest() {
        var reader = HttpReqReader.defaultReader();
        var channel = chunks(REQUEST);
        assertThat(reader.read(channel).bodyString()).isEqualTo("hello, chunked!");
        assertThat(reader.read(channel).reqLine()).hasToString("GET /next HTTP/1.1");
        assertThat(reader.read(channel)).isNull();
    }

    @Test
    void splitAtEveryPosition() {
        IntStream.range(1, REQUEST.length()).forEach(split -> {
            var reader = HttpReqReader.defaultReader();
            var channel = chunks(REQUEST.substring(0, split), REQUEST.substring(split));
            assertThat(reader.read(channel).bodyString()).describedAs("split %d", split).isEqualTo("hello, chunked!");
            assertThat(reader.read(channel).reqLine()).describedAs("split %d", split).hasToString("GET /next HTTP/1.1");
        });
    }

    @Test
    void oneByteAtATime() {
        var reader = HttpReqReader.defaultReader();
        var channel = chunks(REQUEST.chars().mapToObj(c -> String.valueOf((char) c)).toArray(String[]::new));
        assertThat(reader.read(channel).bodyString()).isEqualTo("hello, chunked!");
        assertThat(reader.read(channel).reqLine()).hasToString("GET /next HTTP/1.1");
    }

    @Test
    void unreadBodyIsDrainedForNextRequest() {
        var reader = HttpReqReader.defaultReader();
        var channel = chunks(REQUEST.substring(0, 60), REQUEST.substring(60));
        reader.read(channel);
        assertThat(reader.reusable()).isFalse();
        assertThat(reader.drain(1024)).isTrue();
        assertThat(reader.read(channel).reqLine()).hasToString("GET /next HTTP/1.1");
    }

    @Test
    void largeChunkedBody() {
        var body = "x".repeat(100_000);
        var request = "POST /c HTTP/1.1\r\nTransfer-Encoding: chunked\r\n\r\n" +
                      Integer.toHexString(body.length()) + "\r\n" + body + "\r\n0\r\n\r\n" +
                      "GET /next HTTP/1.1\r\n\r\n";
        var reader = HttpReqReader.defaultReader();
        var channel = chunks(request);
        assertThat(reader.read(channel).bodyString()).isEqualTo(body);
        assertThat(reader.read(channel).reqLine()).hasToString("GET /next HTTP/1.1");
    }

    @Test
    void invalidChunks() {
        for (var chunked : List.of(
            "zz\r\nhello\r\n0\r\n\r\n",
            "\r\nhello\r\n0\r\n\r\n",
            "5\r\nhelloXX0\r\n\r\n",
            "1000000000000000\r\n",
            "5\r\nhel"
        )) {
            var req = HttpReqReader.defaultReader()
                .read(chunks("POST /c HTTP/1.1\r\nTransfer-Encoding: chunked\r\n\r\n" + chunked));
            assertThatThrownBy(req::bodyString)
                .describedAs(chunked)
                .satisfies(e -> assertThat(HttpReqException.find(e))
                    .hasValueSatisfying(reqException -> assertThat(reqException.status()).isEqualTo(400)));
        }
    }

    @Test
    void continueIsSentBeforeFirstReadFromConnection() {
        var sent = new AtomicInteger();
        var reader = HttpReqReader.defaultReader();
        var channel = chunks(
            "POST /c HTTP/1.1\r\nTransfer-Encoding: chunked\r\nExpect: 100-continue\r\n\r\n",
            "5\r\nhello\r\n0\r\n\r\n"
        );
        HttpReq req = reader.read(channel, sent::incrementAndGet);
        assertThat(sent).hasValue(0);
        assertThat(req.bodyString()).isEqualTo("hello");
        assertThat(sent).hasValue(1);
    }

    /// A channel that returns one chunk per read, then end of stream.
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
