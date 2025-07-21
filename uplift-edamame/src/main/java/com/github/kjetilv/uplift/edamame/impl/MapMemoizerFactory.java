package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.edamame.MapsMemoizer;
import com.github.kjetilv.uplift.edamame.PojoBytes;
import com.github.kjetilv.uplift.hash.Bytes;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.hash.Hashes;

import java.util.function.Supplier;

public final class MapMemoizerFactory {

    public static final PojoBytes HASHCODE = value -> Hashes.bytes(value.hashCode());

    public static final PojoBytes TOSTRING = value -> value.toString().getBytes();

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
        return create(keyHandler, null, kind);
    }

    /**
     * @param handler   Key handler, null means default behaviour
     * @param pojoBytes Pojo bytes
     * @return Map memoizer
     */
    public static <I, K, H extends HashKind<H>> MapsMemoizer<I, K> create(
        KeyHandler<K> handler,
        PojoBytes pojoBytes,
        H kind
    ) {
        return create(handler, pojoBytes, null, kind);
    }

    /**
     * @param pojoBytes Pojo bytes
     * @return Map memoizer
     */
    static <I, K, H extends HashKind<H>> MapsMemoizer<I, K> create(PojoBytes pojoBytes, H kind) {
        return create(null, pojoBytes, null, kind);
    }

    /**
     * @param <I>     Id type
     * @param <K>     Key type
     * @param <H>     Hash kind
     * @param handler Key handler, null means default behaviour
     * @param hasher  Leaf hasher, for testing purposes
     * @return Map memoizer
     */
    static <I, K, H extends HashKind<H>> MapsMemoizer<I, K> create(
        KeyHandler<K> handler,
        PojoBytes pojoBytes,
        LeafHasher<H> hasher,
        H kind
    ) {
        return new MapsMemoizerImpl<>(
            hashBuilderSupplier(kind),
            handler == null ? KeyHandler.defaultHandler() : handler,
            hasher == null
                ? defaultLeafHasher(kind, pojoBytes == null ? PojoBytes.HASHCODE : pojoBytes)
                : hasher,
            kind
        );
    }

    private MapMemoizerFactory() {
    }

    private static <H extends HashKind<H>> Supplier<HashBuilder<Bytes, H>> hashBuilderSupplier(H kind) {
        return () -> com.github.kjetilv.uplift.hash.Hashes.hashBuilder(kind);
    }

    private static <H extends HashKind<H>> LeafHasher<H> defaultLeafHasher(H kind, PojoBytes leaf) {
        return new DefaultLeafHasher<H>(hashBuilderSupplier(kind), leaf);
    }
}
