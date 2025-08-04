package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Token;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class MapClimber<H extends HashKind<H>>
    extends StructureClimber<H>
    implements Callbacks {

    private final Map<String, HashedTree<String, H>> map = new HashMap<>();

    private Token.Field currentField;

    MapClimber(
        H kind,
        Supplier<HashBuilder<byte[], H>> supplier,
        LeafHasher<H> leafHasher,
        boolean preserveNulls,
        Callbacks parent,
        Consumer<HashedTree<String, H>> cacher,
        Consumer<HashedTree<String, H>> onDone
    ) {
        super(
            kind,
            supplier,
            leafHasher,
            preserveNulls,
            cacher,
            onDone,
            parent
        );
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
