package com.github.kjetilv.uplift.jmh;

import module java.base;
import com.github.kjetilv.uplift.json.Json;

/// Reads lines of the bundled JSON Lines data set into maps
final class MemoizerData {

    /// @param lines Maximum number of lines
    /// @return Maps, one per line
    static List<Map<?, ?>> maps(int lines) {
        var resource = Objects.requireNonNull(
            MemoizerData.class.getClassLoader().getResourceAsStream("iso_deliverables_metadata.jsonl.gz"),
            "resource"
        );
        try (
            var reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(resource), StandardCharsets.UTF_8))
        ) {
            var json = Json.instance();
            return reader.lines()
                .limit(lines)
                .<Map<?, ?>>map(line -> (Map<?, ?>) json.read(line))
                .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private MemoizerData() {
    }
}
