package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.Canonicalizer;
import com.github.kjetilv.uplift.edamame.Canonicalizers;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.edamame.PojoBytes;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.JsonSession;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

final class CachingJsonSession<H extends HashKind<H>> implements JsonSession {

    private final HashStrategy<H> hashStrategy;

    private final Canonicalizer<String, H> canonicalizer;

    private final BiConsumer<Hash<H>, Object> collisionHandler;

    CachingJsonSession(
        H kind,
        CachingSettings cachingSettings,
        BiConsumer<Hash<H>, Object> collisionHandler
    ) {
        this.collisionHandler = collisionHandler(cachingSettings, collisionHandler);
        this.canonicalizer = canonicalizer(cachingSettings);
        this.hashStrategy = new DefaultHashStrategy<>(
            kind,
            LeafHasher.create(kind, PojoBytes.UNSUPPORTED),
            preserveNull(cachingSettings)
        );
    }

    @Override
    public Callbacks callbacks(Consumer<Object> onDone) {
        return new ValueClimber<>(hashStrategy, canonicalizer, onDone, collisionHandler);
    }

    private static <H extends HashKind<H>> Canonicalizer<String, H> canonicalizer(CachingSettings cachingSettings) {
        return Canonicalizers.canonicalizer(cachingSettings != null && cachingSettings.collisionsNeverHappen());
    }

    private static boolean preserveNull(CachingSettings cachingSettings) {
        return cachingSettings != null && cachingSettings.preserveNulls();
    }

    private static <H extends HashKind<H>> BiConsumer<Hash<H>, Object> collisionHandler(
        CachingSettings settings,
        BiConsumer<Hash<H>, Object> handler
    ) {
        var collisionsHappen = settings == null || !settings.collisionsNeverHappen();
        var collisionsDontHappen = settings != null && settings.collisionsNeverHappen();
        if (collisionsHappen && handler == null) {
            throw new IllegalStateException("Got no collisionHandler, settings:" + settings);
        }
        if (collisionsDontHappen && handler != null) {
            throw new IllegalStateException("Got a collisionHandler in conflict with " + settings + ": " + handler);
        }
        return handler;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               (collisionHandler == null ? "optimistic" : "collisionHandler:" + collisionHandler) +
               " strategy:" + hashStrategy +
               "]";
    }
}
