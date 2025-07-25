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

    @Test
    void test() {
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

    private static void allSame(Collection<Object> c1, Collection<Object> c2) {
        assertThat(c1).allSatisfy(value ->
            assertThat(c2).allSatisfy(otherValue ->
                assertThat(otherValue).isSameAs(value)));
    }
}