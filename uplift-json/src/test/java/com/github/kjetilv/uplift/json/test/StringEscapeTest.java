package com.github.kjetilv.uplift.json.test;

import module java.base;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.json.io.ReadException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/// Escape sequences in strings and field names, when reading and writing.
class StringEscapeTest {

    @Test
    void decodesNamedEscapes() {
        assertThat(readAll("\"\\\" \\\\ \\/ \\b \\f \\n \\r \\t\""))
            .isEqualTo("\" \\ / \b \f \n \r \t");
    }

    @Test
    void decodesUnicodeEscapes() {
        assertThat(readAll("\"\\u0041\\u00e6\\u20AC\"")).isEqualTo("A\u00e6\u20ac");
    }

    @Test
    void decodesSurrogatePair() {
        assertThat(readAll("\"\\uD83D\\uDE00\"")).isEqualTo("\uD83D\uDE00");
    }

    @Test
    void replacesLoneSurrogates() {
        assertThat(readAll("\"\\uD83Dx\"")).isEqualTo("\uFFFDx");
        assertThat(readAll("\"\\uDE00\"")).isEqualTo("\uFFFD");
        assertThat(readAll("\"\\uD83D\\u0041\"")).isEqualTo("\uFFFDA");
    }

    @Test
    void keepsUnescapedUtf8() {
        assertThat(readAll("\"\u00e6\u00f8\u00e5 \uD83D\uDE00\"")).isEqualTo("\u00e6\u00f8\u00e5 \uD83D\uDE00");
    }

    /// Lenient, as before: an unknown escaped character is kept as it is
    @Test
    void keepsUnknownEscapedCharacters() {
        assertThat(readAll("\"\\x\\{\"")).isEqualTo("x{");
        assertThat(readAll("\"\\\n\"")).isEqualTo("\n");
    }

    @Test
    void rejectsMalformedUnicodeEscapes() {
        assertThatThrownBy(() -> JSON.read("\"\\u12G4\"")).isInstanceOf(ReadException.class);
        assertThatThrownBy(() -> JSON.read("\"\\u12\"")).isInstanceOf(ReadException.class);
    }

    @Test
    void rejectsRawControlCharacters() {
        assertThatThrownBy(() -> JSON.read("\"a\tb\"")).isInstanceOf(ReadException.class);
        assertThatThrownBy(() -> JSON.read("{\"a\nb\": 1}")).isInstanceOf(ReadException.class);
    }

    @Test
    void rejectsUnterminatedString() {
        assertThatThrownBy(() -> JSON.read("\"abc"))
            .isInstanceOf(ReadException.class)
            .hasMessageContaining("Unterminated");
    }

    @Test
    void decodesEscapesInFieldNames() {
        assertThat(readAll("{\"a\\\"b\": 1, \"c\\nd\": 2, \"\\u0041\": 3}"))
            .isEqualTo(Map.of("a\"b", 1L, "c\nd", 2L, "A", 3L));
    }

    @Test
    void writesEscapedValues() {
        var value = "x\ny\t\u0001\"\\\b\f\r";
        var written = JSON.write(Map.of("a", value));

        assertThat(written).isEqualTo("{\"a\":\"x\\ny\\t\\u0001\\\"\\\\\\b\\f\\r\"}");
        assertThat(JSON.read(written)).isEqualTo(Map.of("a", value));
    }

    @Test
    void writesEscapedKeys() {
        var key = "k\"\n\\";
        var written = JSON.write(Map.of(key, 1L));

        assertThat(written).isEqualTo("{\"k\\\"\\n\\\\\":1}");
        assertThat(JSON.read(written)).isEqualTo(Map.of(key, 1L));
    }

    @Test
    void roundTripsAllControlCharacters() {
        var value = IntStream.range(0, 0x20)
            .mapToObj(Character::toString)
            .collect(Collectors.joining()) + "\u007f\u00e6\uD83D\uDE00";
        var written = JSON.write(List.of(value));

        assertThat(written.chars().filter(c -> c < 0x20)).isEmpty();
        assertThat(readAll(written)).isEqualTo(List.of(value));
    }

    private static final Json JSON = Json.instance();

    /// Reads through all byte source kinds, and checks that they agree.
    private static Object readAll(String json) {
        var bytes = json.getBytes(StandardCharsets.UTF_8);
        var fromString = JSON.read(json);
        assertThat(JSON.read(bytes)).isEqualTo(fromString);
        assertThat(JSON.read(new ByteArrayInputStream(bytes))).isEqualTo(fromString);
        assertThat(JSON.read(ByteBuffer.wrap(bytes))).isEqualTo(fromString);
        assertThat(JSON.read(MemorySegment.ofArray(bytes))).isEqualTo(fromString);
        return fromString;
    }
}
