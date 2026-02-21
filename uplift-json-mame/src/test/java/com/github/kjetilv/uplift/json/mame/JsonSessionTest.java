package com.github.kjetilv.uplift.json.mame;

import module java.base;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Json;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonSessionTest {

    @SuppressWarnings("unchecked")
    @Test
    void testLists() {
        var reference = new AtomicReference<>();
        var jsonSession = CachingJsonSessions.create(HashKind.K256);
        Json.instance().parse(
            //language=json
            """
                [
                  { "foo": "bar" },
                  { "foo": "bar" }
                ]
                """,
            jsonSession.callbacks(reference::set)
        );
        assertThat(reference).hasValueSatisfying(value ->
            assertThat(value).asInstanceOf(InstanceOfAssertFactories.LIST)
                .satisfies(JsonSessionTest::sameValues));

        var objects = (List<Object>) reference.get();
        var foobar = (Map<String, Object>) objects.getFirst();

        var reference2 = new AtomicReference<Object>();
        Json.instance().parse(
            //language=json
            """
                {
                  "zip" : [
                      { "foo": "bar" },
                      { "foo": "bar" }
                  ]
                }
                """,
            jsonSession.callbacks(reference2::set)
        );
        var zip = (Map<String, Object>) reference2.get();
        var list = (List<Map<String, Object>>) zip.get("zip");
        assertThat(list)
            .satisfies(JsonSessionTest::sameValues)
            .allSatisfy(m -> assertThat(m).isSameAs(foobar));
    }

    @Test
    void testListsSession() {
        var reference = new AtomicReference<Object>();
        var jsonSession = CachingJsonSessions.create(HashKind.K256);
        var json = Json.instance(jsonSession);

        var string =
            //language=json
            """
                [
                  { "foo": "bar" },
                  { "foo": "bar" }
                ]
                """;
        Json.instance().parse(string, jsonSession.callbacks(reference::set));

        var sharedSessionList = json.array(string);
        assertThat(reference).hasValueSatisfying(value ->
            assertThat(value).asInstanceOf(InstanceOfAssertFactories.LIST)
                .satisfies(list ->
                    assertThat(list).isSameAs(sharedSessionList)));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testMaps() {
        var reference = new AtomicReference<Object>();
        var jsonSession = CachingJsonSessions.create(HashKind.K256);
        Json.instance().parse(
            //language=json
            """
                {
                  "zip": { "foo": "bar" },
                  "zot": { "foo": "bar" }
                }
                """,
            jsonSession.callbacks(reference::set)
        );
        assertThat(reference).hasValueSatisfying(value ->
            assertThat(value).asInstanceOf(InstanceOfAssertFactories.MAP)
                .satisfies(JsonSessionTest::sameValues));

        var foobar = ((Map<String, Object>) reference.get()).get("zip");

        var reference2 = new AtomicReference<Object>();
        Json.instance().parse(
            //language=json
            """
                {
                  "a": { "foo": "bar" },
                  "b": { "foo": "bar" }
                }
                """,
            jsonSession.callbacks(reference2::set)
        );
        assertThat(reference2).hasValueSatisfying(value ->
            assertThat(value).asInstanceOf(InstanceOfAssertFactories.MAP)
                .satisfies(map -> {
                    allSame(map.values(), map.values());
                    allSame(map.values(), List.of(foobar));
                }));

        var reference3 = new AtomicReference<Object>();
        Json.instance().parse(
            //language=json
            """
                {
                  "a": { "foo": "bar" },
                  "b": { "foo": "bar" }
                }
                """,
            jsonSession.callbacks(reference3::set)
        );
        assertThat(reference3).hasValueSatisfying(r3 ->
            assertThat(r3).isSameAs(reference2.get()));
    }

    @Test
    void testMapsSession() {
        var reference = new AtomicReference<Object>();
        var jsonSession = CachingJsonSessions.create(HashKind.K256);
        var json =
            //language=json
            """
                {
                  "zip": { "foo": "bar" },
                  "zot": { "foo": "bar" }
                }
                """;
        Json.instance().parse(json, jsonSession.callbacks(reference::set));
        var sharedSessionMap = Json.instance(jsonSession)
            .map(json);
        assertThat(reference).hasValueSatisfying(value ->
            assertThat(value).asInstanceOf(InstanceOfAssertFactories.MAP)
                .satisfies(map ->
                    assertThat(map).isSameAs(sharedSessionMap)));

    }

    private static void sameValues(Map<Object, Object> m) {
        allSame(m.values(), m.values());
    }

    private static void sameValues(Collection<?> c1) {
        allSame(c1, c1);
    }

    private static void allSame(Collection<?> c1, Collection<?> c2) {
        assertThat(c1).allSatisfy(value ->
            assertThat(c2).allSatisfy(otherValue ->
                assertThat(otherValue).isSameAs(value)));
    }
}