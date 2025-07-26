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

    private final Supplier<HashBuilder<byte[], H>> supplier;

    private final LeafHasher<H> leafHasher;

    private final KeyHandler<Token.Field> keyHandler;

    private final Callbacks parent;

    private final Consumer<HashedTree<String, H>> cacher;

    private final Consumer<HashedTree<String, H>> onDone;

    private final Map<String, HashedTree<String, H>> map = new java.util.HashMap<>();

    private final HashBuilder<byte[], H> builder;

    private Token.Field field;

    public MapClimber(
        Supplier<HashBuilder<byte[], H>> supplier,
        LeafHasher<H> leafHasher,
        Callbacks parent,
        Consumer<HashedTree<String, H>> cacher,
        Consumer<HashedTree<String, H>> onDone
    ) {
        this.supplier = supplier;
        this.keyHandler = key1 -> (Token.Field) key1;
        this.leafHasher = leafHasher;
        this.parent = parent;
        this.cacher = cacher;
        this.onDone = onDone;

        this.builder = this.supplier.get();
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
            supplier,
            leafHasher,
            this,
            cacher,
            this::set
        );
    }

    @Override
    public Callbacks bool(boolean bool) {
        return set(leaf(bool));
    }

    @Override
    public Callbacks number(Token.Number number) {
        return set(leaf(number.number()));
    }

    @Override
    public Callbacks string(Token.Str str) {
        return set(leaf(str.value()));
    }

    @Override
    public Callbacks arrayStarted() {
        return new ListClimber<>(supplier, leafHasher, this, cacher, this::set);
    }

    @Override
    public Callbacks objectEnded() {
        onDone.accept(new HashedTree.Node<>(builder.get(), map));
        return parent;
    }

    private Callbacks set(HashedTree<String, H> tree) {
        try {
            map.put(field.value(), tree);
        } finally {
            cacher.accept(tree);
        }
        return this;
    }

    private HashedTree.Leaf<String, H> leaf(Object value) {
        return TreeClimber.leaf(leafHasher, value);
    }
}
