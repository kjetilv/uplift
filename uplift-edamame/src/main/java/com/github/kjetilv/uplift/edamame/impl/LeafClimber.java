package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.HashedTreeClimber;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.Objects;
import java.util.function.Consumer;

public record LeafClimber<K, H extends HashKind<H>>(
    LeafHasher<H> leafHasher,
    HashedTreeClimber<K, H> parent,
    Consumer<HashedTree<K, H>> cacher
) implements HashedTreeClimber<K, H> {

    public LeafClimber(
        LeafHasher<H> leafHasher,
        HashedTreeClimber<K, H> parent,
        Consumer<HashedTree<K, H>> cacher
    ) {
        this.leafHasher = Objects.requireNonNull(leafHasher, "leafHasher");
        this.parent = Objects.requireNonNull(parent, "parent");
        this.cacher = Objects.requireNonNull(cacher, "onDone");
    }

    @Override
    public HashedTreeClimber<K, H> leaf(Object value) {
        Hash<H> hash = leafHasher.hash(leafHasher);
        cacher.accept(new HashedTree.Leaf<>(hash, value));
        return parent;
    }
}
