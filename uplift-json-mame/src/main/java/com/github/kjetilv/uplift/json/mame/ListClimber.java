package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ListClimber<H extends HashKind<H>> implements Callbacks {

    private final Supplier<HashBuilder<byte[], H>> supplier;

    private final LeafHasher<H> leafHasher;

    private final Callbacks parent;

    private final Consumer<HashedTree<String, H>> cacher;

    private final Consumer<HashedTree<String, H>> onDone;

    private final List<HashedTree<String, H>> list = new ArrayList<>();

    private final HashBuilder<byte[], H> builder;

    public ListClimber(
        Supplier<HashBuilder<byte[], H>> supplier,
        LeafHasher<H> leafHasher,
        Callbacks parent,
        Consumer<HashedTree<String, H>> cacher,
        Consumer<HashedTree<String, H>> onDone
    ) {
        this.supplier = supplier;
        this.leafHasher = leafHasher;
        this.parent = parent;
        this.cacher = cacher;
        this.onDone = onDone;

        this.builder = this.supplier.get();
    }

    @Override
    public Callbacks objectStarted() {
        return new MapClimber<>(
            supplier,
            leafHasher,
            this,
            cacher,
            this::add
        );
    }

    @Override
    public Callbacks arrayStarted() {
        return new ListClimber<>(
            supplier,
            leafHasher,
            this,
            cacher,
            this::add
        );
    }

    @Override
    public Callbacks bool(boolean bool) {
        return add(bool);
    }

    @Override
    public Callbacks number(Token.Number number) {
        return add(number.number());
    }

    @Override
    public Callbacks string(Token.Str str) {
        return add(str.value());
    }

    @Override
    public Callbacks arrayEnded() {
        onDone.accept(new HashedTree.Nodes<>(builder.get(), list));
        return parent;
    }

    private Callbacks add(Object object) {
        add(TreeClimber.tree(leafHasher, object));
        return this;
    }

    private void add(HashedTree.Leaf<String, H> tree) {
        try {
            list.add(tree);
        } finally {
            cacher.accept(tree);
        }
    }
}
