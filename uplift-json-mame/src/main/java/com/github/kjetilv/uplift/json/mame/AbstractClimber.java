package com.github.kjetilv.uplift.json.mame;

import module java.base;
import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Token;

abstract sealed class AbstractClimber<H extends HashKind<H>>
    implements Climber
    permits StructureClimber, ValueClimber {

    private final KeyHandler<String> keyHandler;

    private final Consumer<HashedTree<String, H>> cacher;

    private final HashStrategy<H> hashStrategy;

    AbstractClimber(
        HashStrategy<H> hashStrategy,
        KeyHandler<String> keyHandler,
        Consumer<HashedTree<String, H>> cacher
    ) {
        this.hashStrategy = Objects.requireNonNull(hashStrategy, "climbingStrategy");
        this.keyHandler = Objects.requireNonNull(keyHandler, "keyHandler");
        this.cacher = Objects.requireNonNull(cacher, "cacher");
    }

    @Override
    public final Callbacks arrayStarted() {
        return new ListClimber<>(hashStrategy, keyHandler, this, cacher, this::done);
    }

    @Override
    public final Callbacks objectStarted() {
        return new MapClimber<>(hashStrategy, keyHandler, this, cacher, this::done);
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

    protected void cache(HashedTree<String, H> tree) {
        cacher.accept(tree);
    }

    protected String normalized(Token.Field key) {
        return keyHandler.normalize(key);
    }

    protected abstract void done(HashedTree<String, H> tree);

    private Callbacks doneLeaf(Object object) {
        done(new HashedTree.Leaf<>(hashStrategy.hashLeaf(object), object));
        return this;
    }
}
