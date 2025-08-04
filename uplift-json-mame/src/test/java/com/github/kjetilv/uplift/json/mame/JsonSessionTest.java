package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.json.JsonSession;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class JsonSessionTest {

    @SuppressWarnings("unchecked")
    @Test
    void testLists() {
        AtomicReference<Object> reference = new AtomicReference<>();
        JsonSession jsonSession = JsonSessions.create(HashKind.K256);
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

        List<Object> objects = (List<Object>) reference.get();
        Map<String, Object> foobar = (Map<String, Object>) objects.getFirst();

        AtomicReference<Object> reference2 = new AtomicReference<>();
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
        Map<String, Object> zip = (Map<String, Object>) reference2.get();
        List<Map<String, Object>> list = (List<Map<String, Object>>) zip.get("zip");
        assertThat(list)
            .satisfies(JsonSessionTest::sameValues)
            .allSatisfy(m -> assertThat(m).isSameAs(foobar));
    }

    @Test
    void testListsSession() {
        AtomicReference<Object> reference = new AtomicReference<>();
        JsonSession jsonSession = JsonSessions.create(HashKind.K256);
        String json =
            //language=json
            """
                [
                  { "foo": "bar" },
                  { "foo": "bar" }
                ]
                """;
        Json.instance().parse(json, jsonSession.callbacks(reference::set));

        List<?> sharedSessionList = Json.instance(jsonSession).jsonArray(json);
        assertThat(reference).hasValueSatisfying(value ->
            assertThat(value).asInstanceOf(InstanceOfAssertFactories.LIST)
                .satisfies(list ->
                    assertThat(list).isSameAs(sharedSessionList)));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testMaps() {
        AtomicReference<Object> reference = new AtomicReference<>();
        JsonSession jsonSession = JsonSessions.create(HashKind.K256);
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

        Object foobar = ((Map<String, Object>) reference.get()).get("zip");

        AtomicReference<Object> reference2 = new AtomicReference<>();
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

        AtomicReference<Object> reference3 = new AtomicReference<>();
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
        AtomicReference<Object> reference = new AtomicReference<>();
        JsonSession jsonSession = JsonSessions.create(HashKind.K256);
        String json =
            //language=json
            """
            {
              "zip": { "foo": "bar" },
              "zot": { "foo": "bar" }
            }
            """;
        Json.instance().parse(json, jsonSession.callbacks(reference::set));
        Map<?, ?> sharedSessionMap = Json.instance(jsonSession).jsonMap(json);
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