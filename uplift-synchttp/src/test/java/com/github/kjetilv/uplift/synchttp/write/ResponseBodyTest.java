package com.github.kjetilv.uplift.synchttp.write;

import module java.base;
import com.github.kjetilv.uplift.synchttp.rere.HttpRes;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/// Tests for response bodies: length, encoding, and size.
class ResponseBodyTest {

    @Test
    void largeBodyFromChannel() {
        var out = new ByteArrayOutputStream();
        HttpResponseCallback.create(Channels.newChannel(out), ByteBuffer.allocate(4096))
            .status(200)
            .contentLength(20_000)
            .body(Channels.newChannel(new ByteArrayInputStream(new byte[20_000])));
        assertThat(out.size()).isEqualTo("HTTP/1.1 200 OK\r\ncontent-length: 20000\r\n\r\n".length() + 20_000);
    }

    @Test
    void largeBodyFromChannelInSmallReads() {
        var out = new ByteArrayOutputStream();
        HttpResponseCallback.create(Channels.newChannel(out), ByteBuffer.allocate(4096))
            .status(200)
            .contentLength(20_000)
            .body(smallReads(new byte[20_000], 100));
        assertThat(out.size()).isEqualTo("HTTP/1.1 200 OK\r\ncontent-length: 20000\r\n\r\n".length() + 20_000);
    }

    @Test
    void wrongBodyLengthFailsBeforeWriting() {
        var out = new ByteArrayOutputStream();
        assertThatThrownBy(() ->
            HttpResponseCallback.create(Channels.newChannel(out), ByteBuffer.allocate(4096))
                .status(200)
                .contentLength(5)
                .body("abc")
        ).isInstanceOf(IllegalStateException.class);
        assertThat(out.size()).isZero();
    }

    @Test
    void httpResWriterLargeBody() {
        var out = new ByteArrayOutputStream();
        new HttpResWriter(Channels.newChannel(out))
            .write(new HttpRes(200, 20_000, List.of(), smallReads(new byte[20_000], 100)));
        assertThat(out.size()).isEqualTo("HTTP/1.1 200 OK\r\ncontent-length: 20000\r\n\r\n".length() + 20_000);
    }

    @Test
    void httpResStringBodyLengthIsInBytes() {
        var out = new ByteArrayOutputStream();
        new HttpResWriter(Channels.newChannel(out)).write(new HttpRes(200, "blåbær"));
        assertThat(out.toString(UTF_8)).isEqualTo("HTTP/1.1 200 OK\r\ncontent-length: 8\r\n\r\nblåbær");
    }

    /// A channel that returns at most `chunk` bytes per read
    private static ReadableByteChannel smallReads(byte[] bytes, int chunk) {
        var source = ByteBuffer.wrap(bytes);
        return new ReadableByteChannel() {

            @Override
            public int read(ByteBuffer dst) {
                if (!source.hasRemaining()) {
                    return -1;
                }
                var count = Math.min(chunk, Math.min(dst.remaining(), source.remaining()));
                dst.put(source.slice(source.position(), count));
                source.position(source.position() + count);
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
