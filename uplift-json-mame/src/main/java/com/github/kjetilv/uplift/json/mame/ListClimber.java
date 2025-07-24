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

    private final Supplier<HashBuilder<byte[], H>> hashBuilderSupplier;

    private final LeafHasher<H> leafHasher;

    private final Callbacks parent;

    private final Consumer<HashedTree<Token.Field, H>> cacher;

    private final Consumer<HashedTree<Token.Field, H>> onDone;

    private final List<HashedTree<Token.Field, H>> list = new ArrayList<>();

    private final HashBuilder<byte[], H> builder;

    public ListClimber(
        Supplier<HashBuilder<byte[], H>> hashBuilderSupplier,
        LeafHasher<H> leafHasher,
        Callbacks parent,
        Consumer<HashedTree<Token.Field, H>> cacher,
        Consumer<HashedTree<Token.Field, H>> onDone
    ) {
        this.hashBuilderSupplier = hashBuilderSupplier;
        this.leafHasher = leafHasher;
        this.parent = parent;
        this.cacher = cacher;
        this.onDone = onDone;

        this.builder = this.hashBuilderSupplier.get();
    }

    @Override
    public Callbacks objectStarted() {
        return new MapClimber<>(
            hashBuilderSupplier,
            leafHasher,
            this,
            cacher,
            this::added
        );
    }

    @Override
    public Callbacks arrayStarted() {
        return new ListClimber<>(
            hashBuilderSupplier,
            leafHasher,
            this,
            cacher,
            this::added
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
        HashedTree<Token.Field, H> added = added(TreeClimber.tree(leafHasher, object));
        cacher.accept(added);
        return this;
    }

    private HashedTree<Token.Field, H> added(HashedTree<Token.Field, H> tree) {
        try {
            list.add(tree);
            return tree;
        } finally {
            cacher.accept(tree);
        }
    }
}
