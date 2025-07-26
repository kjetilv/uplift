package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.CanonicalValue;
import com.github.kjetilv.uplift.edamame.Canonicalizer;
import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;

import java.util.function.Consumer;
import java.util.function.Supplier;

final class TreeClimber<H extends HashKind<H>> extends AbstractClimber<H> {

    private final Consumer<Object> onDone;

    private final Canonicalizer<String, H> canonicalizer;

    TreeClimber(
        Supplier<HashBuilder<byte[], H>> supplier,
        LeafHasher<H> leafHasher,
        Canonicalizer<String, H> canonicalizer,
        Consumer<Object> onDone
    ) {
        super(supplier, leafHasher, canonicalizer::canonical);
        this.canonicalizer = canonicalizer;
        this.onDone = onDone;
    }

    @Override
    protected Callbacks done(HashedTree<String, H> tree) {
        CanonicalValue<H> canonical = canonicalizer.canonical(tree);
        onDone.accept(canonical.value());
        return this;
    }
}
