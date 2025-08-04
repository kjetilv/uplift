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
    implements Callbacks permits StructureClimber, ValueClimber {

    protected final Consumer<HashedTree<String, H>> cacher;

    private final H kind;

    private final Supplier<HashBuilder<byte[], H>> supplier;

    private final LeafHasher<H> leafHasher;

    private final boolean preserveNulls;

    protected AbstractClimber(
        H kind,
        Supplier<HashBuilder<byte[], H>> supplier,
        LeafHasher<H> leafHasher,
        boolean preserveNulls,
        Consumer<HashedTree<String, H>> cacher
    ) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.supplier = Objects.requireNonNull(supplier, "supplier");
        this.leafHasher = Objects.requireNonNull(leafHasher, "leafHasher");
        this.preserveNulls = preserveNulls;
        this.cacher = Objects.requireNonNull(cacher, "cacher");
    }

    @Override
    public final Callbacks arrayStarted() {
        return new ListClimber<>(
            kind,
            supplier,
            leafHasher,
            preserveNulls,
            this,
            cacher,
            this::done
        );
    }

    @Override
    public final Callbacks objectStarted() {
        return new MapClimber<>(
            kind,
            supplier,
            leafHasher,
            preserveNulls,
            this,
            cacher,
            this::done
        );
    }

    @Override
    public final Callbacks bool(boolean bool) {
        return doneLeaf(bool);
    }

    @Override
    public final Callbacks number(Token.Number number) {
        return doneLeaf(number.number());
    }

    @Override
    public final Callbacks string(Token.Str str) {
        return doneLeaf(str.value());
    }

    @Override
    public Callbacks nuul() {
        if (preserveNulls) {
            done(HashedTree.Null.instanceFor(kind));
        }
        return this;
    }

    protected abstract void done(HashedTree<String, H> tree);

    private Callbacks doneLeaf(Object object) {
        done(leaf(object));
        return this;
    }

    private HashedTree<String, H> leaf(Object object) {
        return new HashedTree.Leaf<>(leafHasher.hash(object), object);
    }

    private static final KeyHandler<Token.Field> KEY_HANDLER = new KeyHandler<>() {

        @Override
        public Token.Field normalize(Object key) {
            return (Token.Field) key;
        }

        @Override
        public byte[] bytes(Token.Field key) {
            return key.bytes();
        }
    };

    protected static byte[] fieldBytes(Token.Field field) {
        return KEY_HANDLER.bytes(field);
    }

    protected static Token.Field normalized(Token.Field key) {
        return KEY_HANDLER.normalize(key);
    }
}
