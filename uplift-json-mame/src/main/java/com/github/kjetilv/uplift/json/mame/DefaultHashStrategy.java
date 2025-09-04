package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.hash.Hashes;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

record DefaultHashStrategy<H extends HashKind<H>>(
    H kind,
    Supplier<HashBuilder<byte[], H>> supplier,
    LeafHasher<H> leafHasher,
    boolean preserveNulls
) implements HashStrategy<H> {

    DefaultHashStrategy {
        requireNonNull(kind, "kind");
        requireNonNull(supplier, "supplier");
        requireNonNull(leafHasher, "leafHasher");
    }

    DefaultHashStrategy(H kind, LeafHasher<H> leafHasher, boolean preserveNulls) {
        this(
            requireNonNull(kind, "kind"),
            () -> Hashes.bytesBuilder(kind),
            leafHasher,
            preserveNulls
        );
    }

    @Override
    public Hash<H> hashLeaf(Object object) {
        return leafHasher.hash(object);
    }

    @Override
    public HashedTree<String, H> getNull() {
        return HashedTree.Null.instanceFor(kind);
    }
}
