package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class ListClimber<H extends HashKind<H>>
    extends SubClimber<H> {

    private final List<HashedTree<String, H>> list = new ArrayList<>();

    ListClimber(
        H kind,
        Supplier<HashBuilder<byte[], H>> supplier,
        LeafHasher<H> leafHasher,
        Callbacks parent,
        Consumer<HashedTree<String, H>> cacher,
        Consumer<HashedTree<String, H>> onDone
    ) {
        super(
            kind,
            supplier,
            leafHasher,
            cacher,
            onDone,
            parent
        );
    }

    @Override
    public Callbacks arrayEnded() {
        return close();
    }

    @Override
    protected HashedTree<String, H> hashedTree(Hash<H> hash) {
        return new HashedTree.Nodes<>(hash, list);
    }

    @Override
    protected void set(HashedTree<String, H> tree) {
        list.add(tree);
    }
}
