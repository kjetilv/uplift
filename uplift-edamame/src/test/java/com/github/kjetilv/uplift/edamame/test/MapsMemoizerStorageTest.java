package com.github.kjetilv.uplift.edamame.test;

import module java.base;
import com.github.kjetilv.uplift.edamame.CanonicalValue;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.edamame.MapsMemoizer;
import com.github.kjetilv.uplift.edamame.MapsMemoizers;
import com.github.kjetilv.uplift.hash.HashKind.K128;
import org.junit.jupiter.api.Test;

import static com.github.kjetilv.uplift.edamame.impl.InternalFactory.create;
import static com.github.kjetilv.uplift.hash.HashKind.K128;
import static com.github.kjetilv.uplift.hash.HashKind.K256;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/// Storage behaviour of [MapsMemoizer], including the overflow path taken on hash collisions.
@SuppressWarnings("unchecked")
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

    @Test
    void collisionStoresCopy() {
        MapsMemoizer<Long, String> memoizer = create(null, collidingLeafHasher(), K128, null);
        var input = new HashMap<String, Object>(Map.of("a", "2"));

        memoizer.put(1L, Map.of("a", "1"));
        memoizer.put(2L, input);
        input.put("b", "changed after put");

        assertThat(memoizer.get(2L)).isEqualTo(Map.of("a", "2"));
    }

    @Test
    void collisionStoresUnmodifiableNestedStructures() {
        MapsMemoizer<Long, String> memoizer = create(null, collidingLeafHasher(), K128, null);
        var nestedList = new ArrayList<Object>(List.of("x"));
        var nestedMap = new HashMap<String, Object>(Map.of("c", "y"));

        memoizer.put(1L, Map.of("a", "1"));
        memoizer.put(2L, Map.of("a", nestedList, "b", nestedMap, "e", List.of()));
        nestedList.add("changed after put");
        nestedMap.put("d", "changed after put");

        var stored = memoizer.get(2L);
        assertThat(stored).isEqualTo(Map.of("a", List.of("x"), "b", Map.of("c", "y")));
        assertThatThrownBy(() -> ((List<Object>) stored.get("a")).add("z"))
            .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> ((Map<String, Object>) stored.get("b")).put("z", "z"))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void putIfAbsentOnCollisionKeepsExistingId() {
        MapsMemoizer<Long, String> memoizer = create(null, collidingLeafHasher(), K128, null);

        memoizer.put(1L, Map.of("a", "1"));

        assertThat(memoizer.putIfAbsent(1L, Map.of("a", "2")))
            .describedAs("Adding a map with same hash should not work")
            .isFalse();
        assertThat(memoizer.size()).isEqualTo(1);
        assertThat(memoizer.get(1L)).isEqualTo(Map.of("a", "1"));
    }

    @Test
    void putOnCollisionRejectsDuplicateId() {
        MapsMemoizer<Long, String> memoizer = create(null, collidingLeafHasher(), K128, null);

        memoizer.put(1L, Map.of("a", "1"));
        memoizer.put(2L, Map.of("a", "2"));

        assertThatThrownBy(() -> memoizer.put(2L, Map.of("a", "3")))
            .describedAs("Should not accept a different value for existing key")
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(memoizer.size()).isEqualTo(2);
    }

    @Test
    void canonicalPutOfOverflowIdIsRejected() {
        MapsMemoizer<Long, String> memoizer = create(null, collidingLeafHasher(), K128, null);

        memoizer.put(1L, Map.of("a", "1"));
        memoizer.put(2L, Map.of("a", "2"));

        assertThat(memoizer.putIfAbsent(2L, Map.of("a", "1"))).isFalse();
        assertThatThrownBy(() -> memoizer.put(2L, Map.of("a", "1")))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(memoizer.size()).isEqualTo(2);
        assertThat(memoizer.get(2L)).isEqualTo(Map.of("a", "2"));
        assertThat(memoizer.maps(true).size()).isEqualTo(2);
    }

    @Test
    void keysNormalizingToSameKeyAreRejected() {
        var memoizer = MapsMemoizers.<Long, K128>create(K128);
        var map = new LinkedHashMap<Object, Object>();
        map.put(1, "x");
        map.put("1", "y");

        assertThatThrownBy(() -> memoizer.put(1L, map))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("1")
            .hasMessageContaining("Integer")
            .hasMessageContaining("String");
        assertThat(memoizer.size()).isZero();
    }

    @Test
    void emptyValuesDoNotCountAsDuplicateKeys() {
        var memoizer = MapsMemoizers.<Long, K128>create(K128);
        var map = new LinkedHashMap<Object, Object>();
        map.put(1, List.of());
        map.put("1", "y");

        memoizer.put(1L, map);

        assertThat(memoizer.get(1L)).isEqualTo(Map.of("1", "y"));
    }

    @Test
    void liveViewSizeFollowsMemoizer() {
        var memoizer = MapsMemoizers.<Long, K128>create(K128);
        memoizer.put(1L, Map.of("a", "1"));

        var view = memoizer.maps(false);
        memoizer.put(2L, Map.of("a", "2"));

        assertThat(view.size()).isEqualTo(2);
        assertThat(view.get(2L)).isEqualTo(Map.of("a", "2"));
    }

    @Test
    void liveViewSeesLaterOverflow() {
        MapsMemoizer<Long, String> memoizer = create(null, collidingLeafHasher(), K128, null);
        memoizer.put(1L, Map.of("a", "1"));

        var view = memoizer.maps(false);
        memoizer.put(2L, Map.of("a", "2"));

        assertThat(view.size()).isEqualTo(2);
        assertThat(view.get(2L)).isEqualTo(Map.of("a", "2"));
    }

    @Test
    void copyIsNotAffectedByLaterPuts() {
        var memoizer = MapsMemoizers.<Long, K128>create(K128);
        memoizer.put(1L, Map.of("a", "1"));

        var copy = memoizer.maps(true);
        memoizer.put(2L, Map.of("a", "2"));

        assertThat(copy.size()).isEqualTo(1);
        assertThat(copy.get(2L)).isNull();
    }

    @Test
    void nullCanonicalValueHasRequestedKind() {
        assertThat(CanonicalValue.Null.instanceFor(K256).hash().kind()).isEqualTo(K256);
        assertThat(CanonicalValue.Null.instanceFor(K128).hash().kind()).isEqualTo(K128);
    }

    /// @return A map: `{a: null, b: 1, c: {d: null}, e: value}`
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
