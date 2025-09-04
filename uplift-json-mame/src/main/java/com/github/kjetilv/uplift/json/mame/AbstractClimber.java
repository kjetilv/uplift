package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Token;

import java.util.Objects;
import java.util.function.Consumer;

sealed abstract class AbstractClimber<H extends HashKind<H>>
    implements Climber
    permits StructureClimber, ValueClimber {

    protected final Consumer<HashedTree<String, H>> cacher;

    private final HashStrategy<H> hashStrategy;

    protected AbstractClimber(HashStrategy<H> hashStrategy, Consumer<HashedTree<String, H>> cacher) {
        this.hashStrategy = Objects.requireNonNull(hashStrategy, "climbingStrategy");
        this.cacher = Objects.requireNonNull(cacher, "cacher");
    }

    @Override
    public final Callbacks arrayStarted() {
        return new ListClimber<>(hashStrategy, this, cacher, this::done);
    }

    @Override
    public final Callbacks objectStarted() {
        return new MapClimber<>(hashStrategy, this, cacher, this::done);
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
        if (hashStrategy.preserveNulls()) {
            done(hashStrategy.getNull());
        }
        return this;
    }

    protected abstract void done(HashedTree<String, H> tree);

    private Callbacks doneLeaf(Object object) {
        done(new HashedTree.Leaf<>(hashStrategy.hashLeaf(object), object));
        return this;
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
