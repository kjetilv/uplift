package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.CanonicalValue;
import com.github.kjetilv.uplift.edamame.Canonicalizer;
import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class Climber<H extends HashKind<H>>
    extends AbstractClimber<H> {

    private final Consumer<Object> onDone;

    private final BiConsumer<Hash<H>, Object> collisionHandler;

    private final Canonicalizer<String, H> canonicalizer;

    Climber(
        Supplier<HashBuilder<byte[], H>> supplier,
        LeafHasher<H> leafHasher,
        Canonicalizer<String, H> canonicalizer,
        Consumer<Object> onDone,
        BiConsumer<Hash<H>, Object> collisionHandler
    ) {
        super(supplier, leafHasher, canonicalizer::canonical);
        this.canonicalizer = Objects.requireNonNull(canonicalizer, "canonicalizer");
        this.onDone = Objects.requireNonNull(onDone, "onDone");
        this.collisionHandler = collisionHandler;
    }

    @Override
    protected void done(HashedTree<String, H> tree) {
        CanonicalValue<H> canonical = canonicalizer.canonical(tree);
        if (canonical instanceof CanonicalValue.Collision<H>(Hash<H> hash, Object value)) {
            if (collisionHandler == null) {
                throw new IllegalStateException("Unexpected collision: " + hash + " -> " + value);
            }
            collisionHandler.accept(hash, value);
        }
        onDone.accept(canonical.value());
    }
}
