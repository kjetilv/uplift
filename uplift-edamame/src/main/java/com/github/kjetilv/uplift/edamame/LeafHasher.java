package com.github.kjetilv.uplift.edamame;

import module java.base;
import com.github.kjetilv.uplift.edamame.impl.DefaultLeafHasher;
import com.github.kjetilv.uplift.edamame.impl.InternalFactory;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.util.Bytes;

/// Strategy for hashing leaf values.  [Overridable][InternalFactory#create(KeyHandler, LeafHasher, HashKind, PojoBytes)]
/// for testing purposes.
@FunctionalInterface
public interface LeafHasher<K extends HashKind<K>> {

    static <K extends HashKind<K>> LeafHasher<K> create(K kind) {
        return create(kind, null, null);
    }

    static <K extends HashKind<K>> LeafHasher<K> create(K kind, PojoBytes pojoBytes) {
        return create(kind, null, pojoBytes);
    }

    static <K extends HashKind<K>> LeafHasher<K> create(K kind, Supplier<HashBuilder<Bytes, K>> supplier) {
        return create(kind, supplier, null);
    }

    static <K extends HashKind<K>> LeafHasher<K> create(
        K kind,
        Supplier<HashBuilder<Bytes, K>> supplier,
        PojoBytes pojoBytes
    ) {
        return new DefaultLeafHasher<>(
            supplier == null ? () -> HashBuilder.forKind(kind) : supplier,
            pojoBytes == null ? PojoBytes.UNSUPPORTED : pojoBytes
        );
    }

    /// @param leaf Leaf value
    /// @return Hash
    Hash<K> hash(Object leaf);
}
