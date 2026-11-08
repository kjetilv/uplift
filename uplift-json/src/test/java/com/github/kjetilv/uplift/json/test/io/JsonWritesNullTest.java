package com.github.kjetilv.uplift.json.test.io;

import module java.base;
import com.github.kjetilv.uplift.json.Json;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/// Map entries that count as null are left out, and maps with no other entries are written as null. In
/// lists, such values are written as null, to keep the indexes of the other elements.
class JsonWritesNullTest {

    @Test
    void emptyMapIsNull() {
        assertWritten(Map.of(), "null");
    }

    @Test
    void mapWithOnlyNullsIsNull() {
        assertWritten(map("a", null), "null");
        assertWritten(map("a", null, "b", Optional.empty(), "c", Map.of(), "d", map("e", null)), "null");
        assertWritten(map("a", Optional.of(Map.of())), "null");
    }

    @Test
    void nullEntriesAreLeftOut() {
        assertWritten(map("a", null, "b", 1, "c", Map.of()), "{\"b\":1}");
        assertWritten(map("a", map("b", null, "c", true)), "{\"a\":{\"c\":true}}");
    }

    @Test
    void emptyListsAreKept() {
        assertWritten(map("l", List.of()), "{\"l\":[]}");
    }

    @Test
    void nullsInListsKeepTheirIndex() {
        assertWritten(
            Arrays.asList(null, Map.of(), map("a", null), Optional.empty(), 1),
            "[null,null,null,null,1]"
        );
        assertWritten(map("l", Arrays.asList(map("a", null), map("b", 2))), "{\"l\":[null,{\"b\":2}]}");
    }

    private static void assertWritten(Object value, String expected) {
        assertThat(Json.instance().write(value)).isEqualTo(expected);
        assertThat(new String(Json.instance().writeBytes(value), UTF_8)).isEqualTo(expected);
    }

    private static Map<String, Object> map(Object... keysAndValues) {
        var map = new LinkedHashMap<String, Object>();
        for (var i = 0; i < keysAndValues.length; i += 2) {
            map.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }
}
