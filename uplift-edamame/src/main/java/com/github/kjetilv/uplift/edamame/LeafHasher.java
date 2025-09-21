package com.github.kjetilv.uplift.edamame;

import module java.base;
import module uplift.hash;

import com.github.kjetilv.uplift.edamame.impl.DefaultLeafHasher;
import com.github.kjetilv.uplift.edamame.impl.InternalFactory;

/// Strategy for hashing leaf values.  [Overridable][InternalFactory#create(KeyHandler, LeafHasher, HashKind, PojoBytes)]
/// for testing purposes.
@FunctionalInterface
public interface LeafHasher<H extends HashKind<H>> {

    static <H extends HashKind<H>> LeafHasher<H> create(H kind) {
        return create(kind, null, null);
    }

    static <H extends HashKind<H>> LeafHasher<H> create(
        H kind,
        PojoBytes pojoBytes
    ) {
        return create(kind, null, pojoBytes);
    }

    static <H extends HashKind<H>> LeafHasher<H> create(
        H kind,
        Supplier<HashBuilder<byte[], H>> supplier
    ) {
        return create(kind, supplier, null);
    }

    static <H extends HashKind<H>> LeafHasher<H> create(
        H kind,
        Supplier<HashBuilder<byte[], H>> supplier,
        PojoBytes pojoBytes
    ) {
        return new DefaultLeafHasher<>(
            supplier == null ? () -> Hashes.bytesBuilder(kind) : supplier,
            pojoBytes == null ? PojoBytes.UNSUPPORTED : pojoBytes
        );
    }

    /// @param leaf Leaf value
    /// @return Hash
    Hash<H> hash(Object leaf);
}
