package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.Canonicalizer;
import com.github.kjetilv.uplift.edamame.Canonicalizers;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.edamame.PojoBytes;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.hash.Hashes;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.JsonSession;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class CachingJsonSession<H extends HashKind<H>> implements JsonSession {

    private final Climber.Strategy<H> strategy;

    private final Canonicalizer<String, H> canonicalizer;

    private final BiConsumer<Hash<H>, Object> collisionHandler;

    CachingJsonSession(
        H kind,
        boolean preserveNulls,
        boolean collisionsNeverHappen,
        BiConsumer<Hash<H>, Object> collisionHandler
    ) {
        if (collisionsNeverHappen && collisionHandler != null) {
            throw new IllegalStateException(
                "Got a collisionHandler even though collisionsNeverHappen=" + collisionsNeverHappen +
                ": " + collisionHandler
            );
        }
        Supplier<HashBuilder<byte[], H>> supplier = () -> Hashes.bytesBuilder(kind);
        LeafHasher<H> leafHasher = LeafHasher.create(kind, supplier, PojoBytes.UNSUPPORTED);

        this.collisionHandler = collisionHandler;
        this.canonicalizer = Canonicalizers.canonicalizer(collisionsNeverHappen);
        this.strategy = new Climber.Strategy<>(kind, supplier, leafHasher, preserveNulls);
    }

    @Override
    public Callbacks callbacks(Consumer<Object> onDone) {
        return new ValueClimber<>(strategy, canonicalizer, onDone, collisionHandler);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               (collisionHandler == null ? "optimistic" : "collisionHandler:" + collisionHandler) +
               " strategy:" + strategy +
               "]";
    }
}
