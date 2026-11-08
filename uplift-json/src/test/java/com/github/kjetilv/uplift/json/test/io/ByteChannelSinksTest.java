package com.github.kjetilv.uplift.json.test.io;

import module java.base;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.json.io.BufferedByteChannelSink;
import com.github.kjetilv.uplift.json.io.ChunkedTransferByteChannelSink;
import com.github.kjetilv.uplift.json.io.JsonWrites;
import com.github.kjetilv.uplift.json.io.Sink;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/// The buffered and chunked channel sinks must write all output for any buffer size, including writes larger
/// than the buffer.
class ByteChannelSinksTest {

    @Test
    void bufferedWritesEverything() {
        BUFFER_SIZES.forEach(bufferSize ->
            VALUES.forEach(value ->
                assertThat(new String(
                    write(value, channel -> new BufferedByteChannelSink(channel, null, bufferSize)),
                    UTF_8
                ))
                    .describedAs("Buffer size %d", bufferSize)
                    .isEqualTo(Json.instance().write(value))));
    }

    @Test
    void chunkedWritesEverything() {
        BUFFER_SIZES.forEach(bufferSize ->
            VALUES.forEach(value -> {
                var chunked = write(value, channel -> new ChunkedTransferByteChannelSink(channel, null, bufferSize));
                assertThat(dechunk(chunked, bufferSize))
                    .describedAs("Buffer size %d", bufferSize)
                    .isEqualTo(Json.instance().write(value));
            }));
    }

    @Test
    void chunkedWritesNoEmptyChunkBeforeTheEnd() {
        var value = Map.of("k", "x".repeat(100));
        var chunked = new String(
            write(value, channel -> new ChunkedTransferByteChannelSink(channel, null, 10)),
            UTF_8
        );
        assertThat(chunked).startsWith("6\r\n{\"k\":\"\r\n");
        assertThat(chunked.indexOf("\r\n0\r\n")).isEqualTo(chunked.length() - "\r\n0\r\n\r\n".length());
    }

    private static final List<Integer> BUFFER_SIZES = Stream.concat(
        IntStream.rangeClosed(0, 40).boxed(),
        Stream.of(255, 256, 1024, 4095, 4096, 8192)
    ).toList();

    private static final List<Object> VALUES = List.of(
        Map.of(),
        Map.of("k", "v"),
        sequenced(
            "short", "s",
            "long", "a".repeat(5000),
            "multibyte", "æ€😀".repeat(700),
            "escapes", "\"\\\n\u0001".repeat(300),
            "list", List.of(1, "two", Long.MIN_VALUE, "x".repeat(300))
        )
    );

    private static Map<String, Object> sequenced(Object... keysAndValues) {
        var map = new LinkedHashMap<String, Object>();
        for (var i = 0; i < keysAndValues.length; i += 2) {
            map.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }

    private static byte[] write(Object value, Function<WritableByteChannel, Sink> sink) {
        var out = new ByteArrayOutputStream();
        try (
            var channel = Channels.newChannel(out);
            var s = sink.apply(channel)
        ) {
            JsonWrites.write(s, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }

    /// Decodes chunked transfer coding, checking the framing: every chunk is non-empty, and the stream ends
    /// with exactly one empty chunk.
    private static String dechunk(byte[] chunked, int bufferSize) {
        var data = new ByteArrayOutputStream();
        var position = 0;
        while (true) {
            var lineEnd = indexOfCrlf(chunked, position);
            assertThat(lineEnd).describedAs("Chunk header at %d, buffer size %d", position, bufferSize)
                .isGreaterThan(position);
            var size = Integer.parseInt(new String(chunked, position, lineEnd - position, UTF_8), 16);
            position = lineEnd + 2;
            if (size == 0) {
                assertThat(position + 2).describedAs("Terminator, buffer size %d", bufferSize)
                    .isEqualTo(chunked.length);
                assertThat(new String(chunked, position, 2, UTF_8)).isEqualTo("\r\n");
                return data.toString(UTF_8);
            }
            data.write(chunked, position, size);
            position += size;
            assertThat(new String(chunked, position, 2, UTF_8))
                .describedAs("Chunk end at %d, buffer size %d", position, bufferSize)
                .isEqualTo("\r\n");
            position += 2;
        }
    }

    private static int indexOfCrlf(byte[] bytes, int from) {
        for (var i = from; i < bytes.length - 1; i++) {
            if (bytes[i] == '\r' && bytes[i + 1] == '\n') {
                return i;
            }
        }
        return -1;
    }
}
