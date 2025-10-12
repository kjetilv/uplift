package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.CanonicalValue;
import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.edamame.PojoBytes;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.hash.HashKind.K128;
import com.github.kjetilv.uplift.hash.Hashes;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SuppressWarnings("ResultOfMethodCallIgnored")
class CanonicalSubstructuresCataloguerTest {

    @Test
    void testLeaf() {
        TreeHasher<K, K128> hasher = new RecursiveTreeHasher<>(
            () -> Hashes.hashBuilder(HashKind.K128),
            KeyHandler.defaultHandler(),
            LeafHasher.create(HashKind.K128, PojoBytes.HASHCODE),
            HashKind.K128
        );
        var cataloguer = CanonicalSubstructuresCataloguer.<K, K128>create();
        Supplier<Object> newObject = () -> new BigDecimal("42");

        var cv1 = cataloguer.canonical(hasher.hash(newObject.get()));
        if (cv1 instanceof CanonicalValue.Leaf<K128>(var h1, var o1)) {
            var cv2 = cataloguer.canonical(hasher.hash(newObject.get()));
            if (cv2 instanceof CanonicalValue.Leaf<K128>(var h2, var o2)) {
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
        TreeHasher<K, K128> hasher = new RecursiveTreeHasher<>(
            () -> Hashes.hashBuilder(HashKind.K128),
            key -> new K(key.toString()),
            LeafHasher.create(HashKind.K128, PojoBytes.HASHCODE),
            HashKind.K128
        );
        var cataloguer = CanonicalSubstructuresCataloguer.<K, K128>create();

        var key = copySupplier("foo");
        var val = copySupplier("bar");
        Supplier<Object> newObject = () -> Map.of(key.get(), val.get());

        var cv1 = cataloguer.canonical(hasher.hash(newObject.get()));
        if (cv1 instanceof CanonicalValue.Node<?, K128>(var h1, var m1)) {
            assertThat(m1.keySet()).allMatch(K.class::isInstance);
            var cv2 = cataloguer.canonical(hasher.hash(newObject.get()));
            if (cv2 instanceof CanonicalValue.Node<?, K128>(var h2, var m2)) {
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
    private static Supplier<String> copySupplier(String original) {
        return () -> new String(original);
    }

    public record K(String k) {
    }
}