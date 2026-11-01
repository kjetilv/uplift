package com.github.kjetilv.uplift.edamame.test;

import module java.base;
import com.github.kjetilv.uplift.edamame.CanonicalValue;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.edamame.MapsMemoizer;
import com.github.kjetilv.uplift.edamame.MapsMemoizers;
import com.github.kjetilv.uplift.hash.HashKind.K128;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.github.kjetilv.uplift.edamame.impl.InternalFactory.create;
import static com.github.kjetilv.uplift.hash.HashKind.K128;
import static com.github.kjetilv.uplift.hash.HashKind.K256;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/// Storage behaviour of [MapsMemoizer], including the overflow path taken on hash collisions.
///
/// Disabled tests document known defects, by id from the uplift-edamame review. Remove `@Disabled` when fixed.
class MapsMemoizerStorageTest {

    @Test
    void keepsNullMapValue() {
        var memoizer = MapsMemoizers.<Long, K128>create(K128);

        assertThatNoException().isThrownBy(() -> memoizer.put(1L, mapWithNull("x")));

        var stored = memoizer.get(1L);
        assertThat(stored.keySet()).containsExactlyInAnyOrder("a", "b", "c", "e");
        assertThat(stored.get("a")).isNull();
        assertThat(stored.get("b")).isEqualTo(1);
        assertThat(((Map<?, ?>) stored.get("c")).get("d")).isNull();
    }

    @Test
    void sharesMapsWithNullValues() {
        var memoizer = MapsMemoizers.<Long, K128>create(K128);

        memoizer.put(1L, mapWithNull("x"));
        memoizer.put(2L, mapWithNull("x"));
        memoizer.put(3L, mapWithNull("y"));

        assertThat(memoizer.get(2L)).isSameAs(memoizer.get(1L));
        assertThat(memoizer.get(3L).get("c")).isSameAs(memoizer.get(1L).get("c"));
        assertThat(memoizer.get(3L)).isNotEqualTo(memoizer.get(1L));
    }

    @Disabled("B6: on collision, the raw input map is stored")
    @Test
    void collisionStoresNormalizedKeys() {
        MapsMemoizer<Long, String> memoizer = create(key -> "k:" + key, collidingLeafHasher(), K128, null);

        memoizer.put(1L, Map.of("a", "1"));
        memoizer.put(2L, Map.of("a", "2"));

        assertThat(memoizer.get(1L)).isEqualTo(Map.of("k:a", "1"));
        assertThat(memoizer.get(2L))
            .describedAs("Expected to find colliding map, not the \"canonical\" value")
            .isEqualTo(Map.of("k:a", "2"));
    }

    @Disabled("B6: on collision, the raw input map is stored")
    @Test
    void collisionStoresCopy() {
        MapsMemoizer<Long, String> memoizer = create(null, collidingLeafHasher(), K128, null);
        var input = new HashMap<String, Object>(Map.of("a", "2"));

        memoizer.put(1L, Map.of("a", "1"));
        memoizer.put(2L, input);
        input.put("b", "changed after put");

        assertThat(memoizer.get(2L)).isEqualTo(Map.of("a", "2"));
    }

    @Disabled("B7: overflow path ignores identifier uniqueness")
    @Test
    void putIfAbsentOnCollisionKeepsExistingId() {
        MapsMemoizer<Long, String> memoizer = create(null, collidingLeafHasher(), K128, null);

        memoizer.put(1L, Map.of("a", "1"));

        assertThat(memoizer.putIfAbsent(1L, Map.of("a", "2"))).isFalse();
        assertThat(memoizer.size()).isEqualTo(1);
        assertThat(memoizer.get(1L)).isEqualTo(Map.of("a", "1"));
    }

    @Disabled("B7: overflow path ignores identifier uniqueness")
    @Test
    void putOnCollisionRejectsDuplicateId() {
        MapsMemoizer<Long, String> memoizer = create(null, collidingLeafHasher(), K128, null);

        memoizer.put(1L, Map.of("a", "1"));
        memoizer.put(2L, Map.of("a", "2"));

        assertThatThrownBy(() -> memoizer.put(2L, Map.of("a", "3")))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(memoizer.size()).isEqualTo(2);
    }

    @Disabled("B8: CanonicalValue.Null for K256 is created with K128")
    @Test
    void nullCanonicalValueHasRequestedKind() {
        assertThat(CanonicalValue.Null.instanceFor(K256).hash().kind()).isEqualTo(K256);
        assertThat(CanonicalValue.Null.instanceFor(K128).hash().kind()).isEqualTo(K128);
    }

    /// @return `{a: null, b: 1, c: {d: null}, e: value}`
    private static Map<String, Object> mapWithNull(String value) {
        var inner = new HashMap<String, Object>();
        inner.put("d", null);
        var map = new HashMap<String, Object>();
        map.put("a", null);
        map.put("b", 1);
        map.put("c", inner);
        map.put("e", value);
        return map;
    }

    @SuppressWarnings("unused") // Name parameter, for debugging purposes
    private static LeafHasher<K128> collidingLeafHasher() {
        var collider = K128.random();
        return leaf -> collider;
    }
}
