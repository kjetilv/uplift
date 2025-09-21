package com.github.kjetilv.uplift.json.mame;

import module java.base;
import module uplift.edamame;
import module uplift.hash;

final class ValueClimber<H extends HashKind<H>>
    extends AbstractClimber<H> {

    private final Consumer<Object> onDone;

    private final BiConsumer<Hash<H>, Object> collisionHandler;

    private final Canonicalizer<String, H> canonicalizer;

    ValueClimber(
        HashStrategy<H> hashStrategy,
        Canonicalizer<String, H> canonicalizer,
        Consumer<Object> onDone,
        BiConsumer<Hash<H>, Object> collisionHandler
    ) {
        super(hashStrategy, Objects.requireNonNull(canonicalizer, "canonicalizer")::canonical);
        this.canonicalizer = Objects.requireNonNull(canonicalizer, "canonicalizer");
        this.onDone = Objects.requireNonNull(onDone, "onDone");
        this.collisionHandler = collisionHandler;
    }

    @Override
    protected void done(HashedTree<String, H> tree) {
        CanonicalValue<H> canonical = canonicalizer.canonical(tree);
        if (canonical instanceof CanonicalValue.Collision<H>(Hash<H> hash, Object value)) {
            if (collisionHandler == null) {
                throw new IllegalStateException("Unhandled collision: " + hash + " -> " + value);
            }
            collisionHandler.accept(hash, value);
        }
        onDone.accept(canonical.value());
    }
}
