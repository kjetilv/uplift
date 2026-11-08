package com.github.kjetilv.uplift.jmh;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/// Shared input data and Jackson configuration for the uplift/Jackson comparison benchmarks.
///
/// All data is loaded into memory up front, so that no benchmark measures resource loading.
final class BenchmarkData {

    /// A Jackson mapper configured to match uplift as closely as possible:
    ///
    /// * Unknown properties are ignored, as uplift ignores them
    /// * Nulls and empty collections are not written, as uplift does not write them
    /// * `false` and `0` are written, as uplift writes them
    ///
    /// @return A new mapper
    static ObjectMapper objectMapper() {
        return JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .changeDefaultPropertyInclusion(value ->
                value
                    .withValueInclusion(JsonInclude.Include.NON_EMPTY)
                    .withContentInclusion(JsonInclude.Include.NON_EMPTY))
            .build();
    }

    /// A Jackson mapper for untyped data, configured to match uplift: map entries that are null, or maps with
    /// no non-null entries, are not written.
    ///
    /// `NON_EMPTY` also leaves out empty strings and empty lists, which uplift writes. The bundled data set has
    /// neither, so the output is the same: 267947 bytes for the first 1000 lines, with equal values per line.
    ///
    /// @return A new mapper
    static ObjectMapper mapObjectMapper() {
        return JsonMapper.builder()
            .changeDefaultPropertyInclusion(value ->
                value
                    .withValueInclusion(JsonInclude.Include.NON_EMPTY)
                    .withContentInclusion(JsonInclude.Include.NON_EMPTY))
            .build();
    }

    /// @param resource Classpath resource
    /// @return The bytes of the resource
    static byte[] bytes(String resource) {
        try (var in = open(resource)) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + resource, e);
        }
    }

    /// @param resource Classpath resource with one JSON value per line, optionally gzipped
    /// @param limit Maximum number of lines
    /// @return The non-blank lines, as UTF-8 bytes
    static List<byte[]> lines(String resource, int limit) {
        try (
            var in = open(resource);
            var reader = new BufferedReader(new InputStreamReader(
                resource.endsWith(".gz") ? new GZIPInputStream(in) : in,
                UTF_8
            ))
        ) {
            return reader.lines()
                .filter(line -> !line.isBlank())
                .limit(limit)
                .map(line -> line.getBytes(UTF_8))
                .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + resource, e);
        }
    }

    /// @param values Values to check
    /// @param description What was produced, for the error message
    /// @param <T> Type of value
    /// @return The values, if none are null
    /// @throws IllegalStateException If any value is null
    static <T> List<T> nonNull(List<T> values, String description) {
        var nulls = values.stream().filter(Objects::isNull).count();
        if (nulls > 0) {
            throw new IllegalStateException(description + ": " + nulls + "/" + values.size() + " were null");
        }
        return values;
    }

    private static InputStream open(String resource) {
        return Objects.requireNonNull(
            BenchmarkData.class.getClassLoader().getResourceAsStream(resource),
            () -> "Resource not found: " + resource
        );
    }

    static final String TWEET = "48.json";

    static final String TWEET_LINES = "48.jsonl";

    static final String ISO_LINES = "iso_deliverables_metadata.jsonl.gz";

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private BenchmarkData() {
    }
}
