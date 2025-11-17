package com.github.kjetilv.uplift.edamame;

import module java.base;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.util.Collectioons;

import static com.github.kjetilv.uplift.util.Collectioons.transformList;
import static com.github.kjetilv.uplift.util.Maps.transformMap;

/// A hashed tree mirrors a structure we want to store, decorating each part of the tree with a unique
/// [hash][#hash()].
@SuppressWarnings("unused")
public sealed interface HashedTree<K, H extends HashKind<H>> {

    /// @return The hash of this part of the tree
    Hash<H> hash();

    /// @return The original structure
    Object unwrap();

    /// A node in the tree
    ///
    /// @param hash Hash
    /// @param map  Map
    /// @param <K>  Type of key in the map
    record Node<K, H extends HashKind<H>>(
        Hash<H> hash,
        Map<K, HashedTree<K, H>> map
    ) implements HashedTree<K, H> {

        @Override
        public Object unwrap() {
            return transformMap(map, HashedTree::unwrap);
        }
    }

    /// A list in the tree
    ///
    /// @param hash   Hash
    /// @param values List
    record Nodes<K, H extends HashKind<H>>(
        Hash<H> hash,
        List<HashedTree<K, H>> values
    ) implements HashedTree<K, H> {

        @Override
        public Object unwrap() {
            return Collectioons.transformList(values, HashedTree::unwrap);
        }
    }

    /// A leaf in the tree
    ///
    /// @param hash  Hash
    /// @param value Leaf
    record Leaf<K, H extends HashKind<H>>(
        Hash<H> hash,
        Object value
    ) implements HashedTree<K, H> {

        @Override
        public Object unwrap() {
            return value;
        }
    }

    /// Null value, which may occur in a list. Has the [null][HashKind#blank()] hash.
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

        /// @return null
        @Override
        public Object unwrap() {
            return null;
        }

        /// Cache of null values
        private static final Map<HashKind<?>, Null<?, ?>> NULLS = new ConcurrentHashMap<>();
    }
}
