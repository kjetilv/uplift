package com.github.kjetilv.uplift.json.mame;

import module java.base;
import com.github.kjetilv.uplift.edamame.CanonicalValue;
import com.github.kjetilv.uplift.edamame.Canonicalizer;
import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

final class ValueClimber<H extends HashKind<H>>
    extends AbstractClimber<H> {

    private final Consumer<Object> onDone;

    private final BiConsumer<Hash<H>, Object> collisionHandler;

    private final Canonicalizer<String, H> canonicalizer;

    ValueClimber(
        HashStrategy<H> hashStrategy,
        Canonicalizer<String, H> canonicalizer,
        KeyHandler<String> keyHandler,
        Consumer<Object> onDone,
        BiConsumer<Hash<H>, Object> collisionHandler
    ) {
        super(hashStrategy, keyHandler, Objects.requireNonNull(canonicalizer, "canonicalizer")::canonical);
        this.canonicalizer = Objects.requireNonNull(canonicalizer, "canonicalizer");
        this.onDone = Objects.requireNonNull(onDone, "onDone");
        this.collisionHandler = collisionHandler;
    }

    @Override
    protected void done(HashedTree<String, H> tree) {
        var canonical = canonicalizer.canonical(tree);
        if (canonical instanceof CanonicalValue.Collision<H>(var hash, var value)) {
            if (collisionHandler == null) {
                throw new IllegalStateException("Unhandled collision: " + hash + " -> " + value);
            }
            collisionHandler.accept(hash, value);
        }
        onDone.accept(canonical.value());
    }
}
