package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.edamame.PojoBytes;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.hash.HashKind.K128;
import com.github.kjetilv.uplift.hash.Hashes;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class CanonicalSubstructuresCataloguerTest {

    @Test
    void testLeaf() {
        Canonicalizer<K128> cataloguer = new CanonicalSubstructuresCataloguer<K, K128>(
            new RecursiveTreeHasher<>(
                () -> Hashes.hashBuilder(HashKind.K128),
                KeyHandler.defaultHandler(),
                DefaultLeafHasher.create(HashKind.K128, PojoBytes.HASHCODE),
                HashKind.K128
            )
        );

        Supplier<Object> newObject = () -> new BigDecimal("42");

        CanonicalValue<K128> cv1 = cataloguer.canonical(newObject.get());
        if (cv1 instanceof CanonicalValue.Leaf<K128>(Hash<K128> h1, Object o1)) {
            CanonicalValue<K128> cv2 = cataloguer.canonical(newObject.get());
            if (cv2 instanceof CanonicalValue.Leaf<K128>(Hash<K128> h2, Object o2)) {
                assertThat(h1).isEqualTo(h2);
                assertThat(o1).isSameAs(o2);
            } else {
                throw new IllegalStateException("Unexpected value: " + cv2);
            }
        } else {
            fail("Unexpected value: " + cv1);
        }
    }

    @Test
    void testMap() {
        Canonicalizer<K128> cataloguer = new CanonicalSubstructuresCataloguer<K, K128>(
            new RecursiveTreeHasher<>(
                () -> Hashes.hashBuilder(HashKind.K128),
                key -> new K(key.toString()),
                DefaultLeafHasher.create(HashKind.K128, PojoBytes.HASHCODE),
                HashKind.K128
            )
        );

        Supplier<String> key = supplier("foo");
        Supplier<String> val = supplier("bar");
        Supplier<Object> newObject = () -> Map.of(key.get(), val.get());

        CanonicalValue<K128> cv1 = cataloguer.canonical(newObject.get());
        if (cv1 instanceof CanonicalValue.Node<?, K128>(Hash<K128> h1, Map<?, Object> m1)) {
            assertThat(m1.keySet()).allMatch(K.class::isInstance);
            CanonicalValue<K128> cv2 = cataloguer.canonical(newObject.get());
            if (cv2 instanceof CanonicalValue.Node<?, K128>(Hash<K128> h2, Map<?, Object> m2)) {
                assertThat(h1).isEqualTo(h2);
                assertThat(m1).isSameAs(m2);
                assertThat(m2.keySet()).allMatch(K.class::isInstance);
                assertThat(m1.keySet().iterator().next()).isSameAs(m2.keySet().iterator().next());
                assertThat(m1.values().iterator().next()).isSameAs(m2.values().iterator().next());
            } else {
                fail("Unexpected value: " + cv2);
            }
        } else {
            fail("Unexpected value: " + cv1);
        }
    }

    @SuppressWarnings("StringOperationCanBeSimplified")
    private static Supplier<String> supplier(String bar) {
        return () -> new String(bar);
    }

    public record K(String k) {
    }
}