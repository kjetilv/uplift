package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Token;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

sealed abstract class AbstractClimber<H extends HashKind<H>>
    implements Callbacks permits SubClimber, Climber {

    private final Supplier<HashBuilder<byte[], H>> supplier;

    private final LeafHasher<H> leafHasher;

    private final Consumer<HashedTree<String, H>> cacher;

    public AbstractClimber(
        Supplier<HashBuilder<byte[], H>> supplier,
        LeafHasher<H> leafHasher,
        Consumer<HashedTree<String, H>> cacher
    ) {
        this.supplier = Objects.requireNonNull(supplier, "supplier");
        this.leafHasher = Objects.requireNonNull(leafHasher, "leafHasher");
        this.cacher = Objects.requireNonNull(cacher, "cacher");
    }

    @Override
    public final Callbacks arrayStarted() {
        return new ListClimber<>(supplier, leafHasher, this, cacher, this::done);
    }

    @Override
    public final Callbacks objectStarted() {
        return new MapClimber<>(supplier, leafHasher, this, cacher, this::done);
    }

    @Override
    public final Callbacks bool(boolean bool) {
        done(leaf(bool));
        return this;
    }

    @Override
    public final Callbacks number(Token.Number number) {
        done(leaf(number.number()));
        return this;
    }

    @Override
    public final Callbacks string(Token.Str str) {
        done(leaf(str.value()));
        return this;
    }

    protected final void cache(HashedTree<String, H> tree) {
        cacher.accept(tree);
    }

    protected abstract void done(HashedTree<String, H> tree);

    private HashedTree<String, H> leaf(Object object) {
        return new HashedTree.Leaf<>(leafHasher.hash(object), object);
    }

    private static final KeyHandler<Token.Field> KEY_HANDLER = key -> (Token.Field) key;

    protected static byte[] fieldBytes(Token.Field field) {
        return KEY_HANDLER.bytes(field);
    }

    protected static Token.Field normalized(Token.Field key) {
        return KEY_HANDLER.normalize(key);
    }
}
