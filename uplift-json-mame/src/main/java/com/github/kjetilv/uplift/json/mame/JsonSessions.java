package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.Canonicalizer;
import com.github.kjetilv.uplift.edamame.Canonicalizers;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.hash.Hashes;

import java.util.function.Supplier;

public final class JsonSessions {

    public static <H extends HashKind<H>> JsonSession create(H kind) {
        Supplier<HashBuilder<byte[], H>> supplier = () -> Hashes.bytesBuilder(kind);
        LeafHasher<H> leafHasher = LeafHasher.create(kind, supplier);
        Canonicalizer<String, H> canonicalizer = Canonicalizers.canonicalizer();
        return onDone ->
            new Climber<>(supplier, leafHasher, canonicalizer, onDone);
    }

    private JsonSessions() {
    }
}
