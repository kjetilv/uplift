package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Json;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MameTest {

    @SuppressWarnings("unchecked")
    @Test
    void testLists() {
        AtomicReference<Object> reference = new AtomicReference<>();
        Mame<HashKind.K256> mame = Mame.create(HashKind.K256);
        Json.INSTANCE.parse(
            //language=json
            """
                [
                  { "foo": "bar" },
                  { "foo": "bar" }
                ]
                """,
            mame.onDone(reference::set)
        );
        assertThat(reference).hasValueSatisfying(value ->
            assertThat(value).asInstanceOf(InstanceOfAssertFactories.LIST)
                .satisfies(MameTest::sameValues));

        List<Object> objects = (List<Object>) reference.get();
        Map<String, Object> foobar = (Map<String, Object>) objects.getFirst();

        AtomicReference<Object> reference2 = new AtomicReference<>();
        Json.INSTANCE.parse(
            //language=json
                """
                    {
                      "zip" : [
                          { "foo": "bar" },
                          { "foo": "bar" }
                      ]
                    }
                    """,
            mame.onDone(reference2::set)
        );
        Map<String, Object> zip = (Map<String, Object>) reference2.get();
        List<Map<String, Object>> list = (List<Map<String, Object>>) zip.get("zip");
        assertThat(list).satisfies(MameTest::sameValues);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testMaps() {
        AtomicReference<Object> reference = new AtomicReference<>();
        Mame<HashKind.K256> mame = Mame.create(HashKind.K256);
        Json.INSTANCE.parse(
            //language=json
            """
                {
                  "zip": { "foo": "bar" },
                  "zot": { "foo": "bar" }
                }
                """,
            mame.onDone(reference::set)
        );
        assertThat(reference).hasValueSatisfying(value -> {
            assertThat(value).asInstanceOf(InstanceOfAssertFactories.MAP)
                .satisfies(MameTest::sameValues);
        });

        Object foobar = ((Map<String, Object>) reference.get()).get("zip");

        AtomicReference<Object> reference2 = new AtomicReference<>();
        Json.INSTANCE.parse(
            //language=json
            """
                {
                  "a": { "foo": "bar" },
                  "b": { "foo": "bar" }
                }
                """,
            mame.onDone(reference2::set)
        );
        assertThat(reference2).hasValueSatisfying(value -> {
            assertThat(value).asInstanceOf(InstanceOfAssertFactories.MAP)
                .satisfies(map -> {
                    allSame(map.values(), map.values());
                    allSame(map.values(), List.of(foobar));
                });
        });

        AtomicReference<Object> reference3 = new AtomicReference<>();
        Json.INSTANCE.parse(
            //language=json
            """
                {
                  "a": { "foo": "bar" },
                  "b": { "foo": "bar" }
                }
                """,
            mame.onDone(reference3::set)
        );
        assertThat(reference3).hasValueSatisfying(r3 ->
            assertThat(r3).isSameAs(reference2.get()));
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