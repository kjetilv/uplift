package com.github.kjetilv.uplift.json.mame;

import module java.base;
import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.util.Bytes;

import static java.util.Objects.requireNonNull;

record DefaultHashStrategy<H extends HashKind<H>>(
    H kind,
    Supplier<HashBuilder<Bytes, H>> supplier,
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
            () -> HashBuilder.forKind(kind),
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
