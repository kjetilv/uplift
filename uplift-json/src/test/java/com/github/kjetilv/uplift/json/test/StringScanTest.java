package com.github.kjetilv.uplift.json.test;

import module java.base;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.json.bytes.ByteArrayIntsBytesSource;
import com.github.kjetilv.uplift.json.io.ReadException;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/// Strings are scanned several bytes at a time. Special bytes, and bytes next to the special range, must be
/// found at every offset, from every kind of source.
class StringScanTest {

    @Test
    void findsSpecialsAtEveryOffset() {
        IntStream.rangeClosed(0, 17).forEach(offset ->
            SAMPLES.forEach(sample -> {
                var padding = "a".repeat(offset);
                var expected = padding + sample.decoded() + padding + " \u007f";
                var json = "{\"" + padding + sample.encoded() + "\":\"" + padding + sample.encoded() + padding +
                           " \u007f\"}";
                var bytes = json.getBytes(UTF_8);
                Stream.of(
                    Json.instance().read(bytes),
                    Json.instance().read(new TrickleInputStream(bytes))
                ).forEach(read ->
                    assertThat(read)
                        .describedAs("Offset %d, sample %s", offset, sample.encoded())
                        .isEqualTo(Map.of(padding + sample.decoded(), expected)));
            }));
    }

    @Test
    void rejectsControlCharactersAtEveryOffset() {
        IntStream.rangeClosed(0, 17).forEach(offset -> {
            var bytes = ("\"" + "a".repeat(offset) + "\u001f\"").getBytes(UTF_8);
            assertThatThrownBy(() -> Json.instance().read(bytes))
                .describedAs("Offset %d", offset)
                .isInstanceOf(ReadException.class)
                .hasMessageContaining("Unescaped control character");
        });
    }

    @Test
    void doesNotReadPastTheEndOfARange() {
        IntStream.rangeClosed(0, 17).forEach(length -> {
            var content = "\"" + "a".repeat(length);
            var bytes = (content + "bb\"" + "a".repeat(16)).getBytes(UTF_8);
            var source = new ByteArrayIntsBytesSource(bytes, 0, content.length());
            assertThatThrownBy(() -> Json.instance().read(source))
                .describedAs("Length %d", length)
                .isInstanceOf(ReadException.class)
                .hasMessageContaining("Unterminated string");
        });
    }

    private record Sample(String encoded, String decoded) {
    }

    private static final List<Sample> SAMPLES = List.of(
        new Sample("\\\"", "\""),
        new Sample("\\\\", "\\"),
        new Sample("\\n", "\n"),
        new Sample("\\u001f", "\u001f"),
        new Sample("\\u0041", "A"),
        new Sample("æ", "æ"),
        new Sample("€", "€"),
        new Sample("😀", "😀"),
        new Sample("!#", "!#"),
        new Sample("[]", "[]"),
        new Sample("\u007f", "\u007f")
    );

    /// Returns one to three bytes per read, so that strings cross buffer refills
    private static final class TrickleInputStream extends ByteArrayInputStream {

        private int reads;

        private TrickleInputStream(byte[] bytes) {
            super(bytes);
        }

        @Override
        public synchronized int read(byte[] b, int off, int len) {
            return super.read(b, off, Math.min(len, 1 + reads++ % 3));
        }
    }
}
