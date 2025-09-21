package com.github.kjetilv.uplift.json.mame;

import module java.base;
import module uplift.edamame;
import module uplift.hash;
import module uplift.json;

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
        if (settings != null && settings.collisionsNeverHappen() && handler != null) {
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
