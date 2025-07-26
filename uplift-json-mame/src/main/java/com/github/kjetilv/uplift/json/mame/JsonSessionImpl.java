package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.Canonicalizer;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record JsonSessionImpl<H extends HashKind<H>>(
    Supplier<HashBuilder<byte[], H>> hashBuilderSupplier,
    LeafHasher<H> leafHasher,
    Canonicalizer<String, H> cacher
) implements JsonSession<H> {

    @Override
    public Callbacks onDone(Consumer<Object> onDone) {
        return new TreeClimber<>(hashBuilderSupplier, leafHasher, cacher, onDone);
    }
}
