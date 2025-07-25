package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.Canonicalizer;
import com.github.kjetilv.uplift.edamame.Canonicalizers;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.edamame.PojoBytes;
import com.github.kjetilv.uplift.hash.Bytes;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.hash.Hashes;
import com.github.kjetilv.uplift.json.Callbacks;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record Mame<H extends HashKind<H>>(
    Supplier<HashBuilder<byte[], H>> hashBuilderSupplier,
    LeafHasher<H> leafHasher,
    Canonicalizer<String, H> cacher
) {

    public static <H extends HashKind<H>> Mame<H> create(H kind) {
        Supplier<HashBuilder<byte[], H>> supplier = () -> Hashes.hashBuilder(kind)
            .map(Bytes::from);
        LeafHasher<H> leafHasher = LeafHasher.create(kind, PojoBytes.HASHCODE);
        Canonicalizer<String, H> canonicalizer = Canonicalizers.canonicalizer();
        return new Mame<>(supplier, leafHasher, canonicalizer);
    }

    public static <H extends HashKind<H>> Callbacks climb(H kind, Consumer<Object> onDone) {
        Supplier<HashBuilder<byte[], H>> supplier = () -> Hashes.hashBuilder(kind)
            .map(Bytes::from);
        LeafHasher<H> leafHasher = LeafHasher.create(kind, PojoBytes.HASHCODE);
        Canonicalizer<String, H> canonicalizer = Canonicalizers.canonicalizer();
        return new TreeClimber<>(supplier, leafHasher, canonicalizer, onDone);
    }

    public Callbacks onDone(Consumer<Object> onDone) {
        return new TreeClimber<>(hashBuilderSupplier, leafHasher, cacher, onDone);
    }
}
