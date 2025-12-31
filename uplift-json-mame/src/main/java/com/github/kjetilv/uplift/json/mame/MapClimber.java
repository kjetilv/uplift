package com.github.kjetilv.uplift.json.mame;

import module java.base;
import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Token;

final class MapClimber<H extends HashKind<H>> extends StructureClimber<H> {

    private final Map<String, HashedTree<String, H>> map = new HashMap<>();

    private String currentField;

    MapClimber(
        HashStrategy<H> hashStrategy,
        KeyHandler<String> keyHandler,
        Callbacks parent,
        Consumer<HashedTree<String, H>> cacher,
        Consumer<HashedTree<String, H>> onDone
    ) {
        super(hashStrategy, keyHandler, cacher, onDone, parent);
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
        map.put(currentField, tree);
    }
}
