package com.github.kjetilv.uplift.json.test.io;

import module java.base;
import com.github.kjetilv.uplift.json.FieldEvents;
import com.github.kjetilv.uplift.json.io.DefaultFieldEvents;
import com.github.kjetilv.uplift.json.io.JsonWrites;
import com.github.kjetilv.uplift.json.io.Sink;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/// The stream sink encodes directly into a buffer. Its output must equal the string sink's output, encoded
/// with [String#getBytes(Charset)], including where characters cross the buffer boundary.
class SinkEquivalenceTest {

    @Test
    void writesSameAsStringSink() {
        values().forEach(value -> {
            var expected = toString(sink -> JsonWrites.write(sink, value));
            assertThat(toBytes(UTF_8, sink -> JsonWrites.write(sink, value)))
                .isEqualTo(expected.getBytes(UTF_8));
            assertThat(toBytes(ISO_8859_1, sink -> JsonWrites.write(sink, value)))
                .isEqualTo(expected.getBytes(ISO_8859_1));
        });
    }

    @Test
    void writesFieldsSameAsStringSink() {
        values().forEach(value -> {
            var expected = toString(sink -> fields(sink, value));
            assertThat(toBytes(UTF_8, sink -> fields(sink, value)))
                .isEqualTo(expected.getBytes(UTF_8));
            assertThat(toBytes(ISO_8859_1, sink -> fields(sink, value)))
                .isEqualTo(expected.getBytes(ISO_8859_1));
        });
    }

    /// Strings with every kind of char, placed at every offset around the buffer boundary
    private static Stream<Object> values() {
        var samples = List.of(
            "😀",
            "æ",
            "€",
            "\uD800",
            "\uDC00",
            "x\uD83D",
            "\"",
            "\\",
            "\n",
            "\u0001",
            "\u001f"
        );
        var strings = IntStream.rangeClosed(490, 530)
            .boxed()
            .flatMap(padding -> samples.stream()
                .map(sample -> "a".repeat(padding) + sample + "b"));
        var others = Stream.<Object>of(
            Long.MIN_VALUE,
            Long.MAX_VALUE,
            0L,
            -1,
            (short) -7,
            (byte) 127,
            1.5e-7,
            List.of("æ".repeat(2000), "😀".repeat(1000))
        );
        return Stream.concat(strings, others);
    }

    private static void fields(Sink sink, Object value) {
        FieldEvents events = new DefaultFieldEvents(sink);
        switch (value) {
            case String string -> events.string(string, string).string("k", string);
            case Number number -> events.number("n", number).numberArray("ns", List.of(number, number));
            case List<?> list -> events.stringArray("ss", list.stream().map(String::valueOf).toList());
            default -> throw new IllegalArgumentException(String.valueOf(value));
        }
        events.done();
    }

    private static String toString(Consumer<Sink> writer) {
        var sb = new StringBuilder();
        try (var sink = Sink.string(sb)) {
            writer.accept(sink);
        }
        return sb.toString();
    }

    private static byte[] toBytes(Charset charset, Consumer<Sink> writer) {
        var out = new ByteArrayOutputStream();
        try (var sink = Sink.stream(out, charset)) {
            writer.accept(sink);
        }
        return out.toByteArray();
    }
}
