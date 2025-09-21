package com.github.kjetilv.uplift.json.mame;

import module java.base;
import module uplift.edamame;
import module uplift.hash;
import module uplift.json;

final class ListClimber<H extends HashKind<H>> extends StructureClimber<H> {

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
