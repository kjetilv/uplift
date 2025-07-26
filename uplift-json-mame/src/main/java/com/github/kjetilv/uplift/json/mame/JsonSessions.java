package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.Canonicalizer;
import com.github.kjetilv.uplift.edamame.Canonicalizers;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.edamame.PojoBytes;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.hash.Hashes;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class JsonSessions {

    public static <H extends HashKind<H>> JsonSession create(H kind) {
        return create(
            kind,
            (BiConsumer<Hash<H>, Object>) null
        );
    }

    public static <H extends HashKind<H>> JsonSession create(
        H kind,
        Consumer<Object> collisionHandler
    ) {
        return create(
            kind,
            (_, item) -> collisionHandler.accept(item)
        );
    }

    public static <H extends HashKind<H>> JsonSession create(
        H kind,
        BiConsumer<Hash<H>, Object> collisionHandler
    ) {
        Supplier<HashBuilder<byte[], H>> supplier = () -> Hashes.bytesBuilder(kind);
        LeafHasher<H> leafHasher = LeafHasher.create(kind, supplier, PojoBytes.UNSUPPORTED);
        Canonicalizer<String, H> canonicalizer = Canonicalizers.canonicalizer();
        return onDone ->
            new Climber<>(supplier, leafHasher, canonicalizer, onDone, collisionHandler);
    }

    private JsonSessions() {
    }
}
