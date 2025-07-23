package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.*;
import com.github.kjetilv.uplift.hash.Bytes;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.hash.Hashes;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
        KeyHandler<K> handler = keyHandler != null
            ? keyHandler
            : KeyHandler.defaultHandler();
        LeafHasher<H> hasher = leafHasher != null
            ? leafHasher
            : leafHasher(
                kind,
                pojoBytes == null
                    ? PojoBytes.HASHCODE
                    : pojoBytes
            );
        return new MapsMemoizerImpl<>(
            () -> Hashes.hashBuilder(kind),
            handler,
            hasher,
            kind
        );
    }

    public static <H extends HashKind<H>> LeafHasher<H> leafHasher(H kind, PojoBytes pojoBytes) {
        return pojoBytes.overrideDefaults()
            ? leaf -> Hashes.hash(pojoBytes.bytes(leaf))
            : DefaultLeafHasher.create(kind, pojoBytes);
    }

    public static <H extends HashKind<H>, K> HashedTreeClimber<K, H> climber(
        KeyHandler<K> keyHandler,
        H kind,
        Consumer<HashedTree<K, H>> onDone
    ) {
        Supplier<HashBuilder<byte[], H>> supplier =
            () -> Hashes.hashBuilder(kind)
                .map(Bytes::from);
        LeafHasher<H> leafHasher =
            leafHasher(kind, PojoBytes.HASHCODE);
        return new RootClimber<>(supplier, keyHandler, leafHasher, onDone);
    }

    private MapMemoizerFactory() {
    }
}
