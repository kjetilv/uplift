package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.edamame.impl.DefaultLeafHasher;
import com.github.kjetilv.uplift.edamame.impl.InternalFactory;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.hash.Hashes;

import java.util.function.Supplier;

/**
 * Strategy for hashing leaves.  {@link InternalFactory#create(KeyHandler, LeafHasher, HashKind, PojoBytes) Overridable}
 * for testing purposes.
 */
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
            pojoBytes == null ? PojoBytes.HASHCODE : pojoBytes
        );
    }

    Hash<H> hash(Object leaf);
}
