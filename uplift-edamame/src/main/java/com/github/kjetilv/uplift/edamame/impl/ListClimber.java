package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.HashedTreeClimber;
import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ListClimber<K, H extends HashKind<H>> implements HashedTreeClimber<K, H> {

    private final Supplier<HashBuilder<byte[], H>> hashBuilderSupplier;

    private final LeafHasher<H> leafHasher;

    private final KeyHandler<K> keyHandler;

    private final HashedTreeClimber<K, H> parent;

    private final Consumer<HashedTree<K, H>> cacher;

    private final Consumer<HashedTree<K, H>> onDone;

    private final List<HashedTree<K, H>> list = new ArrayList<>();

    private final HashBuilder<byte[], H> builder;

    public ListClimber(
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
    public HashedTreeClimber<K, H> leaf(Object value) {
        return new LeafClimber<>(leafHasher, this, onDone);
    }

    @Override
    public HashedTreeClimber<K, H> startMap() {
        return new MapClimber<>(
            hashBuilderSupplier,
            keyHandler,
            leafHasher,
            this,
            cacher,
            add()
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
            add()
        );
    }

    @Override
    public HashedTreeClimber<K, H> endList() {
        onDone.accept(new HashedTree.Nodes<>(builder.get(), list));
        return parent;
    }

    private Consumer<HashedTree<K, H>> add() {
        return tree -> {
            try {
                list.add(tree);
            } finally {
                cacher.accept(tree);
            }
        };
    }
}
