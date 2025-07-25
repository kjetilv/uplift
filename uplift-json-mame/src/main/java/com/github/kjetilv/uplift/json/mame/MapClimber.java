package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Token;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MapClimber<H extends HashKind<H>> implements Callbacks {

    private final Supplier<HashBuilder<byte[], H>> hashBuilderSupplier;

    private final LeafHasher<H> leafHasher;

    private final KeyHandler<Token.Field> keyHandler;

    private final Callbacks parent;

    private final Consumer<HashedTree<Token.Field, H>> cacher;

    private final Consumer<HashedTree<Token.Field, H>> onDone;

    private final Map<Token.Field, HashedTree<Token.Field, H>> map = new java.util.HashMap<>();

    private final HashBuilder<byte[], H> builder;

    private Token.Field field;

    public MapClimber(
        Supplier<HashBuilder<byte[], H>> hashBuilderSupplier,
        LeafHasher<H> leafHasher,
        Callbacks parent,
        Consumer<HashedTree<Token.Field, H>> cacher,
        Consumer<HashedTree<Token.Field, H>> onDone
    ) {
        this.hashBuilderSupplier = hashBuilderSupplier;
        this.keyHandler = key1 -> (Token.Field) key1;
        this.leafHasher = leafHasher;
        this.parent = parent;
        this.cacher = cacher;
        this.onDone = onDone;

        this.builder = this.hashBuilderSupplier.get();
    }

    @Override
    public MapClimber<H> field(Token.Field key) {
        this.field = keyHandler.normalize(key);
        builder.hash(keyHandler.bytes(this.field));
        return this;
    }

    @Override
    public Callbacks objectStarted() {
        return new MapClimber<>(
            hashBuilderSupplier,
            leafHasher,
            this,
            cacher,
            this::setField
        );
    }

    @Override
    public Callbacks bool(boolean bool) {
        return set(bool);
    }

    @Override
    public Callbacks number(Token.Number number) {
        return set(number.number());
    }

    @Override
    public Callbacks string(Token.Str str) {
        return set(str.value());
    }

    @Override
    public Callbacks arrayStarted() {
        return new ListClimber<>(
            hashBuilderSupplier,
            leafHasher,
            this,
            cacher,
            this::setField
        );
    }

    @Override
    public Callbacks objectEnded() {
        onDone.accept(new HashedTree.Node<>(builder.get(), map));
        return parent;
    }

    private Callbacks set(Object object) {
        setField(TreeClimber.tree(leafHasher, object));
        return this;
    }

    private void setField(HashedTree<Token.Field, H> tree) {
        try {
            map.put(field, tree);
        } finally {
            cacher.accept(tree);
        }
    }
}
