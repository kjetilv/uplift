package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.*;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.hash.Hashes;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class MapMemoizerFactory {

    /**
     * @param <I>        Id type
     * @param <K>        Key type
     * @param keyHandler Key handler, null means default behaviour
     * @return Map memoizer
     */
    public static <I, K, H extends HashKind<H>> MapsMemoizer<I, K> create(
        KeyHandler<K> keyHandler,
        H kind
    ) {
        return create(keyHandler, kind, null);
    }

    /**
     * @param keyHandler Key handler, null means default behaviour
     * @param pojoBytes  Pojo bytes
     * @return Map memoizer
     */
    public static <I, K, H extends HashKind<H>> MapsMemoizer<I, K> create(
        KeyHandler<K> keyHandler,
        H kind,
        PojoBytes pojoBytes
    ) {
        return create(keyHandler, null, kind, pojoBytes);
    }

    /**
     * @param pojoBytes Pojo bytes
     * @return Map memoizer
     */
    public static <I, K, H extends HashKind<H>> MapsMemoizer<I, K> create(
        H kind,
        PojoBytes pojoBytes
    ) {
        return create(null, null, kind, pojoBytes);
    }

    /**
     * @param <I>        Id type
     * @param <K>        Key type
     * @param <H>        Hash kind
     * @param keyHandler Key handler, null means default behaviour
     * @param leafHasher Leaf hasher, for testing purposes
     * @return Map memoizer
     */
    public static <I, K, H extends HashKind<H>> MapsMemoizer<I, K> create(
        KeyHandler<K> keyHandler,
        LeafHasher<H> leafHasher,
        H kind,
        PojoBytes pojoBytes
    ) {
        Objects.requireNonNull(kind, "kind");
        Canonicalizer<K, H> canonicalValues = canonicalKeys(keyHandler, leafHasher, kind, pojoBytes);
        return new MapsMemoizerImpl<>(canonicalValues);
    }

    public static <H extends HashKind<H>> LeafHasher<H> leafHasher(H kind, PojoBytes pojoBytes) {
        return LeafHasher.create(kind, pojoBytes);
    }

    private MapMemoizerFactory() {
    }

    private static <K, H extends HashKind<H>> Canonicalizer<K, H> canonicalKeys(
        KeyHandler<K> keyHandler,
        LeafHasher<H> leafHasher,
        H kind,
        PojoBytes pojoBytes
    ) {
        KeyHandler<K> handler = keyHandler != null
            ? keyHandler
            : KeyHandler.defaultHandler();
        CanonicalKeysCataloguer<K> canonicalKeys = new CanonicalKeysCataloguer<>(Objects.requireNonNull(
            handler,
            "keyHandler"
        ));
        LeafHasher<H> hasher = leafHasher != null
            ? leafHasher
            : leafHasher(
                kind,
                pojoBytes == null
                    ? PojoBytes.HASHCODE
                    : pojoBytes
            );
        MapHasher<K, H> mapHasher = new RecursiveTreeHasher<>(
            requireNonNull(() -> Hashes.hashBuilder(kind), "newBuilder"),
            canonicalKeys,
            requireNonNull(hasher, "leafHasher"),
            kind
        );
        return new CanonicalSubstructuresCataloguer<>(
            requireNonNull(mapHasher, "mapHasher"));
    }
}
