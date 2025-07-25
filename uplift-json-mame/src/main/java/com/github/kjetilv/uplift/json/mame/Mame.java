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
import com.github.kjetilv.uplift.json.Token;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Mame {

    public static <H extends HashKind<H>> Callbacks climb(H kind, Consumer<Object> onDone) {
        Supplier<HashBuilder<Bytes, H>> supplier = () -> Hashes.hashBuilder(kind);
        LeafHasher<H> leafHasher = LeafHasher.create(kind, PojoBytes.HASHCODE);
        Canonicalizer<Token.Field, H> canonicalizer = Canonicalizers.canonicalizer(
            kind,
            key -> (Token.Field) key,
            leafHasher,
            PojoBytes.HASHCODE
        );
        return new TreeClimber<>(supplier, leafHasher, canonicalizer, onDone);
    }

    private Mame() {
    }
}
