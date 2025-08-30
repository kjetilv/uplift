package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.kjetilv.uplift.util.Collectioons.transform;
import static com.github.kjetilv.uplift.util.Maps.transformValues;

/**
 * A hashed tree mirrors a structure we want to store, decorating each part of the tree with a unique
 * {@link #hash() hash}.
 */
@SuppressWarnings("unused")
public sealed interface HashedTree<K, H extends HashKind<H>> {

    /**
     * @return The hash of this part of the tree
     */
    Hash<H> hash();

    Object unwrap();

    /**
     * A node in the tree
     *
     * @param hash Hash
     * @param map  Map
     * @param <K>  Type of key in the map
     */
    record Node<K, H extends HashKind<H>>(
        Hash<H> hash,
        Map<K, HashedTree<K, H>> map
    ) implements HashedTree<K, H> {

        @Override
        public Object unwrap() {
            return transformValues(map, HashedTree::unwrap);
        }
    }

    /**
     * A list in the tree
     *
     * @param hash   Hash
     * @param values List
     */
    record Nodes<K, H extends HashKind<H>>(
        Hash<H> hash,
        List<HashedTree<K, H>> values
    ) implements HashedTree<K, H> {

        @Override
        public Object unwrap() {
            return transform(values, HashedTree::unwrap);
        }
    }

    /**
     * A leaf in the tree
     *
     * @param hash  Hash
     * @param value Leaf
     */
    record Leaf<K, H extends HashKind<H>>(
        Hash<H> hash,
        Object value
    ) implements HashedTree<K, H> {

        @Override
        public Object unwrap() {
            return value;
        }
    }

    /**
     * Null value, which may occur in a list. Has the {@link HashKind#blank() null} hash.
     */
    record Null<K, H extends HashKind<H>>(Hash<H> hash)
        implements HashedTree<K, H> {

        @SuppressWarnings("unchecked")
        public static <K, H extends HashKind<H>> Null<K, H> instanceFor(H kind) {
            return (Null<K, H>) NULLS.computeIfAbsent(
                kind,
                _ ->
                    new Null<>(kind.blank())
            );
        }

        @Override
        public Object unwrap() {
            return null;
        }

        private static final Map<HashKind<?>, Null<?, ?>> NULLS = new ConcurrentHashMap<>();
    }
}
