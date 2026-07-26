package com.github.kjetilv.uplift.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CaseInsensitiveHashMapTest {

    @Test
    void putAndGet() {
        var foo = Map.of(
            "Foo", 5,
            "Bar", 6
        );
        var cim = CaseInsensitiveHashMap.wrap(foo);

        assertThat(cim.get("Foo")).isEqualTo(5);
        assertThat(cim.get("foo")).isEqualTo(5);
        assertThat(cim.get("FOO")).isEqualTo(5);
    }

}
