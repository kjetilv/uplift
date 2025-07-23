package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.HashedTreeClimber;
import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class RootClimber<K, H extends HashKind<H>> implements HashedTreeClimber<K, H> {

    private final Supplier<HashBuilder<byte[], H>> hashBuilderSupplier;

    private final KeyHandler<K> keyHandler;

    private final LeafHasher<H> leafHasher;

    private final Consumer<HashedTree<K, H>> cacher;

    public RootClimber(
        Supplier<HashBuilder<byte[], H>> hashBuilderSupplier,
        KeyHandler<K> keyHandler,
        LeafHasher<H> leafHasher,
        Consumer<HashedTree<K, H>> cacher
    ) {
        this.hashBuilderSupplier = hashBuilderSupplier;
        this.keyHandler = keyHandler;
        this.leafHasher = leafHasher;
        this.cacher = cacher;
    }

    @Override
    public HashedTreeClimber<K, H> leaf(Object value) {
        return new LeafClimber<>(
            leafHasher,
            this,
            cacher
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
            _ -> {
            }
        );
    }

    public HashedTreeClimber<K, H> startMap() {
        return new MapClimber<>(
            hashBuilderSupplier,
            keyHandler,
            leafHasher,
            this,
            cacher,
            _ -> {
            }
        );
    }
}
