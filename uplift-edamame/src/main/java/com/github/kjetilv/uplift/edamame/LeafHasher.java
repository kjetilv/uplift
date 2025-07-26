package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.edamame.impl.DefaultLeafHasher;
import com.github.kjetilv.uplift.edamame.impl.InternalFactory;
import com.github.kjetilv.uplift.hash.*;

import java.util.function.Supplier;

/**
 * Strategy for hashing leaves.  {@link InternalFactory#create(KeyHandler, LeafHasher, HashKind, PojoBytes) Overridable}
 * for testing purposes.
 */
@FunctionalInterface
public interface LeafHasher<H extends HashKind<H>> {

    static <H extends HashKind<H>> LeafHasher<H> create(H kind) {
        return create(kind, null);
    }

    static <H extends HashKind<H>> LeafHasher<H> create(H kind, PojoBytes pojoBytes) {
        Supplier<HashBuilder<byte[], H>> supplier = () -> Hashes.hashBuilder(kind)
            .map(Bytes::from);
        return new DefaultLeafHasher<>(
            supplier,
            pojoBytes == null ? PojoBytes.HASHCODE : pojoBytes
        );
    }

    default HashBuilder<byte[], H> hashTo(HashBuilder<byte[], H> hb, Object leaf) {
        return hb.hash(hash(leaf).bytes());
    }

    Hash<H> hash(Object leaf);
}
