package com.github.kjetilv.uplift.json.mame;

import module java.base;
import module uplift.edamame;
import module uplift.hash;
import module uplift.json;

final class MapClimber<H extends HashKind<H>> extends StructureClimber<H> {

    private final Map<String, HashedTree<String, H>> map = new HashMap<>();

    private Token.Field currentField;

    MapClimber(
        HashStrategy<H> hashStrategy,
        Callbacks parent,
        Consumer<HashedTree<String, H>> cacher,
        Consumer<HashedTree<String, H>> onDone
    ) {
        super(hashStrategy, cacher, onDone, parent);
    }

    @Override
    public Callbacks field(Token.Field key) {
        this.currentField = hashedField(key);
        return this;
    }

    @Override
    public Callbacks objectEnded() {
        return close();
    }

    @Override
    protected HashedTree<String, H> hashedTree(Hash<H> hash) {
        return new HashedTree.Node<>(hash, map);
    }

    @Override
    protected void set(HashedTree<String, H> tree) {
        map.put(currentField.value(), tree);
    }
}
