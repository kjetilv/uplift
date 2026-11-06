package com.github.kjetilv.uplift.json.test;

import module java.base;
import com.github.kjetilv.uplift.json.BytesSource;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.json.bytes.*;
import com.github.kjetilv.uplift.json.io.ReadException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/// All byte sources must read the same documents to the same values, and fail on the same invalid documents.
/// Streams that return 1 or 7 bytes per read make every token cross a buffer boundary.
class SourceEquivalenceTest {

    @Test
    void sourcesAgreeOnDocuments() {
        documents().forEach(json -> {
            var bytes = json.getBytes(StandardCharsets.UTF_8);
            var expected = JSON.read(bytes);
            sources(bytes).forEach((name, source) ->
                assertThat(read(source.get()))
                    .as("%s: %s", name, json)
                    .isEqualTo(expected));
            assertThat(JSON.read(json.toCharArray())).as("chars: %s", json).isEqualTo(expected);
            assertThat(JSON.read(json)).as("string: %s", json).isEqualTo(expected);
        });
    }

    @Test
    void sourcesAgreeOnErrors() {
        List.of(
                "\"abc", "\"a\\u12\"", "{\"a\": 1.}", "[1,]", "{\"a\" 1}", "troo", "\"a\tb\"", "{} x",
                "[" + "1,".repeat(5000) + "]", "\"" + "x".repeat(20_000)
            )
            .forEach(json -> sources(json.getBytes(StandardCharsets.UTF_8)).forEach((name, source) ->
                assertThat(catchThrowable(() -> read(source.get())))
                    .as("%s: %s", name, json.length() > 40 ? json.substring(0, 40) + "..." : json)
                    .isInstanceOf(ReadException.class)));
    }

    @Test
    void byteBufferReadsFromPositionToLimit() {
        var buffer = ByteBuffer.wrap("xx[1, 2]yy".getBytes(StandardCharsets.UTF_8));
        buffer.position(2).limit(8);
        var direct = ByteBuffer.allocateDirect(10).put("xx[1, 2]yy".getBytes(StandardCharsets.UTF_8));
        direct.position(2).limit(8);

        assertThat(JSON.read(buffer)).isEqualTo(List.of(1L, 2L));
        assertThat(JSON.read(direct)).isEqualTo(List.of(1L, 2L));
        assertThat(buffer.position()).isEqualTo(2);
        assertThat(direct.position()).isEqualTo(2);
    }

    @Test
    void segmentSliceIsReadInPlace() {
        var segment = MemorySegment.ofArray("xx{\"a\": \"b\"}yy".getBytes(StandardCharsets.UTF_8)).asSlice(2, 10);

        assertThat(JSON.read(segment)).isEqualTo(Map.of("a", "b"));
    }

    private static final Json JSON = Json.instance();

    private static List<String> documents() {
        var random = new Random(7);
        var generated = IntStream.range(0, 200)
            .mapToObj(_ -> JSON.write(randomValue(random, 4)));
        var handWritten = Stream.of(
            "\"\\\" \\\\ \\/ \\b \\f \\n \\r \\t \\u0041 \\u00e6 \\u20AC \\uD83D\\uDE00 \\uD83Dx \\x\"",
            "{\"a\\\"b\": 1, \"c\\nd\": [true, false, null], \"\\u0041\": {}}",
            "[0, -0, 1.5, -0.25, 1e5, 1E+5, 1.5e-3, 123456789012345678901234567890, 9223372036854775807, 5]",
            "\uFEFF{\"bom\": true}",
            "  \n\t {\"" + "k".repeat(9000) + "\": \"" + "v\\n".repeat(9000) + "\"}  \n",
            "[\"" + "\u00e6\uD83D\uDE00".repeat(5000) + "\"]",
            "[" + "12345678901234567890.123456789,".repeat(1000) + "0]"
        );
        return Stream.concat(handWritten, generated).toList();
    }

    private static Map<String, Supplier<BytesSource>> sources(byte[] bytes) {
        var sources = new LinkedHashMap<String, Supplier<BytesSource>>();
        sources.put("array", () -> new ByteArrayIntsBytesSource(bytes));
        sources.put("array slice", () -> {
            var padded = new byte[bytes.length + 4];
            System.arraycopy(bytes, 0, padded, 2, bytes.length);
            return new ByteArrayIntsBytesSource(padded, 2, bytes.length);
        });
        sources.put("heap buffer", () -> ByteBufferIntsBytesSource.of(ByteBuffer.wrap(bytes)));
        sources.put("direct buffer", () -> ByteBufferIntsBytesSource.of(
            ByteBuffer.allocateDirect(bytes.length).put(bytes).flip()));
        sources.put("heap segment", () -> MemorySegmentIntsBytesSource.of(MemorySegment.ofArray(bytes)));
        sources.put("native segment", () -> {
            var segment = Arena.ofAuto().allocate(Math.max(1, bytes.length));
            MemorySegment.copy(MemorySegment.ofArray(bytes), 0, segment, 0, bytes.length);
            return MemorySegmentIntsBytesSource.of(segment.asSlice(0, bytes.length));
        });
        sources.put("stream", () -> new InputStreamIntsBytesSource(new ByteArrayInputStream(bytes)));
        sources.put("stream 1 byte", () -> new InputStreamIntsBytesSource(trickle(bytes, 1)));
        sources.put("stream 7 bytes", () -> new InputStreamIntsBytesSource(trickle(bytes, 7)));
        sources.put("channel", () -> new ChannelIntSource(
            Channels.newChannel(trickle(bytes, 3)), bytes.length));
        return sources;
    }

    private static Object read(BytesSource source) {
        return JSON.read(source);
    }

    /// @return Stream that returns at most `chunk` bytes per read
    private static InputStream trickle(byte[] bytes, int chunk) {
        return new ByteArrayInputStream(bytes) {

            @Override
            public synchronized int read(byte[] b, int off, int len) {
                return super.read(b, off, Math.min(len, chunk));
            }
        };
    }

    private static Object randomValue(Random random, int depth) {
        var choice = random.nextInt(depth > 0 ? 9 : 6);
        return switch (choice) {
            case 0 -> randomString(random);
            case 1 -> random.nextLong();
            case 2 -> BigDecimal.valueOf(random.nextLong(), random.nextInt(12));
            case 3 -> random.nextBoolean();
            case 4 -> new BigInteger(100, random);
            case 5 -> random.nextInt(1000);
            case 6, 7 -> {
                var map = new LinkedHashMap<String, Object>();
                IntStream.range(0, random.nextInt(6)).forEach(_ ->
                    map.put(randomString(random), randomValue(random, depth - 1)));
                yield map;
            }
            default -> IntStream.range(0, random.nextInt(5))
                .mapToObj(_ -> randomValue(random, depth - 1))
                .toList();
        };
    }

    private static String randomString(Random random) {
        var alphabet = "ab\"\\/\n\t\r\b\f\u0001\u001f \u00e6\u00f8\u20ac\uD83D\uDE00{}[],:0";
        return random.ints(random.nextInt(30), 0, alphabet.length())
            .mapToObj(i -> {
                var c = alphabet.charAt(i);
                return Character.isHighSurrogate(c) && i + 1 < alphabet.length()
                    ? alphabet.substring(i, i + 2)
                    : Character.isLowSurrogate(c) ? "x" : String.valueOf(c);
            })
            .collect(Collectors.joining());
    }
}
