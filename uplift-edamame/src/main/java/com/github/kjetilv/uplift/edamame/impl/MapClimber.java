package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.HashedTreeClimber;
import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MapClimber<K, H extends HashKind<H>> implements HashedTreeClimber<K, H> {

    private final Supplier<HashBuilder<byte[], H>> hashBuilderSupplier;

    private final LeafHasher<H> leafHasher;

    private final KeyHandler<K> keyHandler;

    private final HashedTreeClimber<K, H> parent;

    private final Consumer<HashedTree<K, H>> cacher;

    private final Consumer<HashedTree<K, H>> onDone;

    private final Map<K, HashedTree<K, H>> map = new java.util.HashMap<>();

    private final HashBuilder<byte[], H> builder;

    private K key;

    public MapClimber(
        Supplier<HashBuilder<byte[], H>> hashBuilderSupplier,
        KeyHandler<K> keyHandler,
        LeafHasher<H> leafHasher,
        HashedTreeClimber<K, H> parent,
        Consumer<HashedTree<K, H>> cacher,
        Consumer<HashedTree<K, H>> onDone
    ) {
        this.hashBuilderSupplier = hashBuilderSupplier;
        this.keyHandler = keyHandler;
        this.leafHasher = leafHasher;
        this.parent = parent;
        this.cacher = cacher;
        this.onDone = onDone;

        this.builder = this.hashBuilderSupplier.get();
    }

    @Override
    public HashedTreeClimber<K, H> field(Object key) {
        this.key = keyHandler.normalize(key);
        builder.hash(keyHandler.bytes(this.key));
        return this;
    }

    @Override
    public HashedTreeClimber<K, H> leaf(Object value) {
        return new LeafClimber<>(
            leafHasher,
            this,
            set()
        );
    }

    @Override
    public HashedTreeClimber<K, H> startMap() {
        return new MapClimber<>(
            hashBuilderSupplier,
            keyHandler,
            leafHasher,
            this,
            cacher,
            set()
        );
    }

    @Override
    public HashedTreeClimber<K, H> startList() {
        return new ListClimber<>(
            hashBuilderSupplier,
            keyHandler,
            leafHasher,
            this,
            cacher,
            set()
        );
    }

    @Override
    public HashedTreeClimber<K, H> endMap() {
        onDone.accept(new HashedTree.Node<>(builder.get(), map));
        return parent;
    }

    private Consumer<HashedTree<K, H>> set() {
        return tree -> {
            try {
                try {
                    map.put(key, tree);
                } finally {
                    key = null;
                }
            } finally {
                cacher.accept(tree);
            }
        };
    }
}
