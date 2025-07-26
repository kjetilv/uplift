package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Token;

import java.util.function.Consumer;
import java.util.function.Supplier;

sealed abstract class AbstractClimber<H extends HashKind<H>> implements Callbacks permits SubClimber, TreeClimber {

    private final LeafHasher<H> leafHasher;

    private final Supplier<HashBuilder<byte[], H>> supplier;

    protected final Consumer<HashedTree<String, H>> cacher;

    public AbstractClimber(
        Supplier<HashBuilder<byte[], H>> supplier,
        LeafHasher<H> leafHasher,
        Consumer<HashedTree<String, H>> cacher
    ) {
        this.leafHasher = leafHasher;
        this.supplier = supplier;
        this.cacher = cacher;
    }

    @Override
    public final Callbacks arrayStarted() {
        return new ListClimber<>(
            supplier,
            leafHasher,
            this,
            cacher,
            this::done
        );
    }

    @Override
    public final Callbacks objectStarted() {
        return new MapClimber<>(
            supplier,
            leafHasher,
            this,
            cacher,
            this::done
        );
    }

    @Override
    public final Callbacks bool(boolean bool) {
        return done(leaf(bool));
    }

    @Override
    public final Callbacks number(Token.Number number) {
        return done(leaf(number.number()));
    }

    @Override
    public final Callbacks string(Token.Str str) {
        return done(leaf(str.value()));
    }

    protected final HashedTree.Leaf<String, H> leaf(Object value) {
        return leaf(leafHasher, value);
    }

    protected abstract Callbacks done(HashedTree<String, H> tree);

    private static final KeyHandler<Token.Field> KEY_HANDLER = key -> (Token.Field) key;

    protected static <H extends HashKind<H>> HashedTree.Leaf<String, H> leaf(
        LeafHasher<H> leafHasher,
        Object object
    ) {
        return new HashedTree.Leaf<>(leafHasher.hash(object), object);
    }

    protected static byte[] fieldBytes(Token.Field field) {
        return KEY_HANDLER.bytes(field);
    }

    protected static Token.Field normalized(Token.Field key) {
        return KEY_HANDLER.normalize(key);
    }
}
