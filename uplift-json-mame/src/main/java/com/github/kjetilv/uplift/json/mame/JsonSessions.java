package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.Canonicalizers;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.hash.Bytes;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.hash.Hashes;

public final class JsonSessions {

    public static <H extends HashKind<H>> JsonSession<H> create(H kind) {
        return new JsonSessionImpl<>(
            () -> Hashes.hashBuilder(kind)
                .map(Bytes::from),
            LeafHasher.create(kind),
            Canonicalizers.canonicalizer()
        );
    }

    private JsonSessions() {
    }
}
