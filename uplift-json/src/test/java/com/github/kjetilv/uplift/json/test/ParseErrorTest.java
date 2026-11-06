package com.github.kjetilv.uplift.json.test;

import module java.base;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.json.ParseException;
import com.github.kjetilv.uplift.json.bytes.ChannelIntSource;
import com.github.kjetilv.uplift.json.callbacks.DefaultJsonSession;
import com.github.kjetilv.uplift.json.io.ReadException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/// Invalid input always fails with a [ReadException], not wrapped, with the position in the input.
class ParseErrorTest {

    @Test
    void invalidInputThrowsReadException() {
        List.of(
                "{\"a\": [1, }}",
                "{\"a\": troo}",
                "{\"a\": \"x",
                "{\"a\": 007}",
                "[1,]",
                "{\"a\" 1}",
                "@",
                "{\"a\": {\"b\": {\"c\": tru}}}",
                "{\"a\": {\"b\": {\"c\": [1 2]}}}",
                "{\"a\": {\"b\": {\"c\": \"\\u12\"}}}"
            )
            .forEach(json -> {
                var thrown = catchThrowable(() -> JSON.read(json));
                assertThat(thrown).as(json).isInstanceOf(ReadException.class);
                assertThat(((ReadException) thrown).position()).as(json).isNotNegative();
                assertThat(thrown.getMessage()).as(json).contains("byte");
            });
    }

    @Test
    void errorCarriesFieldPath() {
        var thrown = catchThrowable(() -> JSON.read("{\"a\": {\"b\": {\"c\": [1, }}}}"));

        assertThat(thrown).isInstanceOf(ParseException.class);
        assertThat(((ReadException) thrown).fieldPath()).containsExactly("a", "b", "c");
        assertThat(thrown.getMessage()).startsWith("Failed to set `a.b.c`: ");
        assertThat(thrown.getCause()).isNull();
    }

    @Test
    void contentAfterValueIsRejected() {
        List.of("1 2", "{} x", "truex", "[1]]", "\"a\" \"b\"", "null,")
            .forEach(json ->
                assertThat(catchThrowable(() -> JSON.read(json)))
                    .as(json)
                    .isInstanceOf(ReadException.class));
        assertThat(catchThrowable(() -> JSON.parse("{} {}", new DefaultJsonSession().callbacks(_ -> {
        }))))
            .isInstanceOf(ReadException.class);
    }

    @Test
    void streamSourceEndsCleanly() {
        assertThat(JSON.read(new ByteArrayInputStream("{\"a\": [1, 2]}  ".getBytes(StandardCharsets.UTF_8))))
            .isEqualTo(Map.of("a", List.of(1L, 2L)));
        assertThat(catchThrowable(() -> JSON.read(new ByteArrayInputStream("{} x".getBytes(StandardCharsets.UTF_8)))))
            .isInstanceOf(ReadException.class);
        assertThat(catchThrowable(() -> JSON.read(new ByteArrayInputStream("\"abc".getBytes(StandardCharsets.UTF_8)))))
            .isInstanceOf(ReadException.class)
            .hasMessageContaining("Unterminated");
    }

    @Test
    void channelSourceDoesNotReadPastLength() {
        var input = new ByteArrayInputStream("{\"a\": 1}NEXT".getBytes(StandardCharsets.UTF_8));
        var reference = new AtomicReference<>();

        JSON.parse(
            new ChannelIntSource(Channels.newChannel(input), 8),
            new DefaultJsonSession().callbacks(reference::set)
        );

        assertThat(reference).hasValue(Map.of("a", 1L));
        assertThat(input.readAllBytes()).asString(StandardCharsets.UTF_8).isEqualTo("NEXT");
    }

    @Test
    void byteOrderMarkIsSkipped() {
        var withBom = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, '{', '"', 'a', '"', ':', '1', '}'};

        assertThat(JSON.read("\uFEFF{\"a\":1}")).isEqualTo(Map.of("a", 1L));
        assertThat(JSON.read(withBom)).isEqualTo(Map.of("a", 1L));
        assertThat(JSON.read(new ByteArrayInputStream(withBom))).isEqualTo(Map.of("a", 1L));
        assertThat(JSON.read(ByteBuffer.wrap(withBom))).isEqualTo(Map.of("a", 1L));
        assertThat(JSON.read(MemorySegment.ofArray(withBom))).isEqualTo(Map.of("a", 1L));
        assertThat(JSON.read("\uFEFF \"x\"")).isEqualTo("x");
    }

    @Test
    void byteOrderMarkElsewhereIsRejected() {
        assertThat(catchThrowable(() -> JSON.read("[\uFEFF1]"))).isInstanceOf(ReadException.class);
        assertThat(catchThrowable(() -> JSON.read(" \uFEFF{}"))).isInstanceOf(ReadException.class);
        assertThat(catchThrowable(() -> JSON.read(new byte[] {(byte) 0xEF, (byte) 0xBB, '{', '}'})))
            .isInstanceOf(ReadException.class);
    }

    @Test
    void nonAsciiFirstByteIsNotEndOfInput() {
        assertThat(catchThrowable(() -> JSON.read("\u00e6")))
            .isInstanceOf(ReadException.class)
            .hasMessageContaining("Unrecognized character");
    }

    @Test
    void whitespaceAfterValueIsAccepted() {
        assertThat(JSON.read(" {\"a\": 1} \n\t\r ")).isEqualTo(Map.of("a", 1L));
        assertThat(JSON.read("true ")).isEqualTo(true);
    }

    @Test
    void nestingUpToLimitIsAccepted() {
        assertThat(JSON.read("[".repeat(1000) + "]".repeat(1000))).isNotNull();
        assertThat(JSON.read("{\"a\":".repeat(999) + "{}" + "}".repeat(999))).isNotNull();
    }

    @Test
    void nestingBeyondLimitIsRejected() {
        List.of(
                "[".repeat(1001) + "]".repeat(1001),
                "{\"a\":".repeat(1000) + "{}" + "}".repeat(1000),
                "[".repeat(100_000) + "]".repeat(100_000),
                "[{\"a\":".repeat(50_000)
            )
            .forEach(json -> {
                var thrown = catchThrowable(() -> JSON.read(json));
                assertThat(thrown).isInstanceOf(ReadException.class);
                assertThat(thrown.getMessage()).contains("Nesting deeper than 1000");
            });
    }

    @Test
    void positionIsNearTheError() {
        var json = "{\"a\": {\"b\": [1, 2, @]}}";
        var thrown = (ReadException) catchThrowable(() -> JSON.read(json));

        assertThat(thrown.position()).isBetween((long) json.indexOf('@') - 1, (long) json.indexOf('@') + 2);
    }

    private static final Json JSON = Json.instance();
}
