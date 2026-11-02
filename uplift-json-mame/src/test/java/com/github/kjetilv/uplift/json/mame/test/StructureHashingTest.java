package com.github.kjetilv.uplift.json.mame.test;

import module java.base;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.json.JsonSession;
import com.github.kjetilv.uplift.json.mame.CachingJsonSessions;
import com.github.kjetilv.uplift.json.mame.CachingSettings;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/// Structure hashes must separate maps, lists and null, ignore key order, and match the stored content.
class StructureHashingTest {

    @Test
    void mapAndListAreKeptApartWithoutCollisionChecks() {
        var session = session(false);
        parse(session, """
            { "k": { "": "x" } }
            """);

        assertThat(parse(session, """
            { "k": [ "x" ] }
            """)).isEqualTo(Map.of("k", List.of("x")));
    }

    @Test
    void emptyMapEmptyListAndNullAreKeptApartWithoutCollisionChecks() {
        var session = session(true);
        parse(session, """
            { "a": {} }
            """);

        assertThat(parse(session, """
            { "a": [] }
            """)).isEqualTo(Map.of("a", List.of()));

        var withNull = (Map<?, ?>) parse(session, """
            { "a": null }
            """);
        assertThat(withNull.containsKey("a")).isTrue();
        assertThat(withNull.get("a")).isNull();
    }

    @Test
    void keyOrderDoesNotAffectSharing() {
        var session = CachingJsonSessions.create(HashKind.K256);

        var first = parse(session, """
            { "a": 1, "b": { "c": 2, "d": 3 } }
            """);
        var second = parse(session, """
            { "b": { "d": 3, "c": 2 }, "a": 1 }
            """);

        assertThat(second).isSameAs(first);
    }

    @Test
    void droppedNullFieldDoesNotAffectSharing() {
        var session = CachingJsonSessions.create(HashKind.K256);

        var first = parse(session, """
            { "a": null, "b": 1 }
            """);
        var second = parse(session, """
            { "b": 1 }
            """);

        assertThat(first).isEqualTo(Map.of("b", 1L));
        assertThat(second).isSameAs(first);
    }

    @Test
    void droppedNullElementDoesNotAffectSharing() {
        var session = CachingJsonSessions.create(HashKind.K256);

        var first = parse(session, """
            { "a": [ 1, null, 2 ] }
            """);
        var second = parse(session, """
            { "a": [ 1, 2 ] }
            """);

        assertThat(second).isSameAs(first);
    }

    @Test
    void duplicateKeyDoesNotAffectSharing() {
        var session = CachingJsonSessions.create(HashKind.K256);

        var first = parse(session, """
            { "a": 1, "a": 2 }
            """);
        var second = parse(session, """
            { "a": 2 }
            """);

        assertThat(first).isEqualTo(Map.of("a", 2L));
        assertThat(second).isSameAs(first);
    }

    private static JsonSession session(boolean preserveNulls) {
        return CachingJsonSessions.create(HashKind.K256, new CachingSettings(preserveNulls, true));
    }

    private static Object parse(JsonSession session, String json) {
        var reference = new AtomicReference<>();
        Json.instance().parse(json, session.callbacks(reference::set));
        return reference.get();
    }
}
