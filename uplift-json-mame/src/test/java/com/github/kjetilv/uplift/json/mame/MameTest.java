package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.json.Token;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MameTest {

    @Test
    void test() {
        AtomicReference<Object> reference = new AtomicReference<>();
        Callbacks climb = Mame.climb(HashKind.K256, newValue -> {
            assertThat(newValue).asInstanceOf(InstanceOfAssertFactories.MAP).isNotEmpty();
            reference.set(newValue);
        });
        Json.INSTANCE.parse(
            //language=json
            """
                {
                  "zip": { "foo": "bar" },
                  "zot": { "foo": "bar" }
                }
                """,
            climb);
        assertThat(reference).hasValueSatisfying(map -> {
            assertThat(map).asInstanceOf(InstanceOfAssertFactories.MAP)
                .containsOnlyKeys("zip", "zot");
        });
    }

}