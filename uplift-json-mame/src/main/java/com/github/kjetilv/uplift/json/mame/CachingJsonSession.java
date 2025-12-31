package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.*;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.JsonSession;
import com.github.kjetilv.uplift.json.Token;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

final class CachingJsonSession<H extends HashKind<H>> implements JsonSession, KeyHandler<String> {

    private final HashStrategy<H> hashStrategy;

    private final Canonicalizer<String, H> canonicalizer;

    private final Map<Object, String> keys = new ConcurrentHashMap<>();

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
    public String normalize(Object key) {
        return keys.computeIfAbsent(key, _ ->
            ((Token.Field)key).value());
    }

    @Override
    public Callbacks callbacks(Consumer<Object> onDone) {
        return new ValueClimber<>(
            hashStrategy,
            canonicalizer,
            this,
            onDone,
            collisionHandler
        );
    }

    private static <H extends HashKind<H>> Canonicalizer<String, H> canonicalizer(CachingSettings settings) {
        return Canonicalizers.canonicalizer(settings != null && settings.collisionsNeverHappen());
    }

    private static boolean preserveNull(CachingSettings cachingSettings) {
        return cachingSettings != null && cachingSettings.preserveNulls();
    }

    private static <H extends HashKind<H>> BiConsumer<Hash<H>, Object> collisionHandler(
        CachingSettings settings,
        BiConsumer<Hash<H>, Object> handler
    ) {
        if ((settings == null || settings.collisionsHappen()) && handler == null) {
            return (hash, object) -> {
                throw new IllegalStateException(object + " collided with hash: " + hash);
            };
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
