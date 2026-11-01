package com.github.kjetilv.uplift.util.test;

import module java.base;
import com.github.kjetilv.uplift.util.Maps;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MapsTest {

    @Test
    void transformMapTransformsValues() {
        assertThat(Maps.transformMap(Map.of("a", 1, "b", 2), i -> i * 10))
            .isEqualTo(Map.of("a", 10, "b", 20));
    }

    @Test
    void transformMapKeepsNullValues() {
        var map = new HashMap<String, Integer>();
        map.put("a", null);
        map.put("b", 2);

        var transformed = Maps.transformMap(map, i -> i == null ? null : i * 10);

        assertThat(transformed).containsOnlyKeys("a", "b");
        assertThat(transformed.get("a")).isNull();
        assertThat(transformed.get("b")).isEqualTo(20);
    }

    @Test
    void transformMapIsUnmodifiable() {
        var transformed = Maps.transformMap(Map.of("a", 1), i -> i);

        assertThatThrownBy(() -> transformed.put("b", 2))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
