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
        return create(keyHandler, kind, null);
    }

    /**
     * @param keyHandler   Key handler, null means default behaviour
     * @param pojoBytes Pojo bytes
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
     * @param <I>     Id type
     * @param <K>     Key type
     * @param <H>     Hash kind
     * @param keyHandler Key handler, null means default behaviour
     * @param leafHasher  Leaf hasher, for testing purposes
     * @return Map memoizer
     */
    public static <I, K, H extends HashKind<H>> MapsMemoizer<I, K> create(
        KeyHandler<K> keyHandler,
        LeafHasher<H> leafHasher,
        H kind,
        PojoBytes pojoBytes
    ) {
        Supplier<HashBuilder<Bytes, H>> builderSupplier = hashBuilderSupplier(kind);
        KeyHandler<K> kh = keyHandler != null ? keyHandler
            : KeyHandler.defaultHandler();
        LeafHasher<H> lh = leafHasher != null ? leafHasher
            : leafHasher(kind, pojoBytes == null ? HASHCODE : pojoBytes);
        return new MapsMemoizerImpl<>(builderSupplier, kh, lh, kind);
    }

    private MapMemoizerFactory() {
    }

    private static <H extends HashKind<H>> Supplier<HashBuilder<Bytes, H>> hashBuilderSupplier(H kind) {
        return () -> Hashes.hashBuilder(kind);
    }

    private static <H extends HashKind<H>> LeafHasher<H> leafHasher(H kind, PojoBytes pojoBytes) {
        return pojoBytes.overrideDefaults()
            ? leaf -> Hashes.hash(pojoBytes.bytes(leaf))
            : new DefaultLeafHasher<>(
                hashBuilderSupplier(kind),
                pojoBytes
            );
    }
}
