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
        CachingSettings cachingSettings,
        BiConsumer<Hash<H>, Object> collisionHandler
    ) {
        if (conflicting(cachingSettings, collisionHandler)) {
            throw new IllegalStateException(
                "Got a collisionHandler in conflict with " + cachingSettings + ": " + collisionHandler);
        } else {
            this.collisionHandler = collisionHandler;
        }
        this.canonicalizer = canonicalizer(cachingSettings);

        Supplier<HashBuilder<byte[], H>> supplier = () -> Hashes.bytesBuilder(kind);
        this.strategy = new Climber.Strategy<>(
            kind,
            supplier,
            LeafHasher.create(kind, supplier, PojoBytes.UNSUPPORTED),
            preserveNull(cachingSettings)
        );
    }

    @Override
    public Callbacks callbacks(Consumer<Object> onDone) {
        return new ValueClimber<>(strategy, canonicalizer, onDone, collisionHandler);
    }

    private static <H extends HashKind<H>> Canonicalizer<String, H> canonicalizer(CachingSettings cachingSettings) {
        return Canonicalizers.canonicalizer(cachingSettings != null && cachingSettings.collisionsNeverHappen());
    }

    private static boolean preserveNull(CachingSettings cachingSettings) {
        return cachingSettings != null && cachingSettings.preserveNulls();
    }

    private static <H extends HashKind<H>> boolean conflicting(
        CachingSettings settings,
        BiConsumer<Hash<H>, Object> handler
    ) {
        return settings != null && settings.collisionsNeverHappen() && handler != null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               (collisionHandler == null ? "optimistic" : "collisionHandler:" + collisionHandler) +
               " strategy:" + strategy +
               "]";
    }
}
