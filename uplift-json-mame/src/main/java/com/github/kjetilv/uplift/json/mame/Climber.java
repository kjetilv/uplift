package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.CanonicalValue;
import com.github.kjetilv.uplift.edamame.Canonicalizer;
import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class Climber<H extends HashKind<H>>
    extends AbstractClimber<H> {

    private final Consumer<Object> onDone;

    private final Canonicalizer<String, H> canonicalizer;

    Climber(
        Supplier<HashBuilder<byte[], H>> supplier,
        LeafHasher<H> leafHasher,
        Canonicalizer<String, H> canonicalizer,
        Consumer<Object> onDone
    ) {
        super(supplier, leafHasher, canonicalizer::canonical);
        this.canonicalizer = Objects.requireNonNull(canonicalizer, "canonicalizer");
        this.onDone = Objects.requireNonNull(onDone, "onDone");
    }

    @Override
    protected void done(HashedTree<String, H> tree) {
        CanonicalValue<H> canonical = canonicalizer.canonical(tree);
        onDone.accept(canonical.value());
    }
}
