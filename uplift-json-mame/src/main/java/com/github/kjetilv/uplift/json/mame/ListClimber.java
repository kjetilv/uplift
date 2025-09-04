package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class ListClimber<H extends HashKind<H>>
    extends StructureClimber<H> {

    private final List<HashedTree<String, H>> list = new ArrayList<>();

    ListClimber(
        HashStrategy<H> hashStrategy,
        Callbacks parent,
        Consumer<HashedTree<String, H>> cacher,
        Consumer<HashedTree<String, H>> onDone
    ) {
        super(hashStrategy, cacher, onDone, parent);
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
