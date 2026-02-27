package com.github.kjetilv.uplift.edamame;

import module java.base;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.util.Collectioons;

import static com.github.kjetilv.uplift.util.Maps.transformMap;

/// A hashed tree mirrors a structure we want to store, decorating each part of the tree with a unique
/// [hash][#hash()].
@SuppressWarnings("unused")
public sealed interface HashedTree<MK, K extends HashKind<K>> {

    /// @return The hash of this part of the tree
    Hash<K> hash();

    /// @return The original structure
    Object unwrap();

    /// A node in the tree
    ///
    /// @param hash Hash
    /// @param map  Map
    /// @param <MK>  Type of key in the map
    record Node<MK, K extends HashKind<K>>(
        Hash<K> hash,
        Map<MK, HashedTree<MK, K>> map
    ) implements HashedTree<MK, K> {

        @Override
        public Object unwrap() {
            return transformMap(map, HashedTree::unwrap);
        }
    }

    /// A list in the tree
    ///
    /// @param hash Hash
    /// @param list List
    record Nodes<MK, K extends HashKind<K>>(
        Hash<K> hash,
        List<HashedTree<MK, K>> list
    ) implements HashedTree<MK, K> {

        @Override
        public Object unwrap() {
            return Collectioons.transformList(list, HashedTree::unwrap);
        }
    }

    /// A leaf in the tree
    ///
    /// @param hash  Hash
    /// @param value Leaf
    record Leaf<MK, K extends HashKind<K>>(
        Hash<K> hash,
        Object value
    ) implements HashedTree<MK, K> {

        @Override
        public Object unwrap() {
            return value;
        }
    }

    /// Null value, which may occur in a list. Has the [null][HashKind#blank()] hash.
    record Null<MK, K extends HashKind<K>>(HashKind<K> kind)
        implements HashedTree<MK, K> {

        @SuppressWarnings("unchecked")
        public static <K, H extends HashKind<H>> Null<K, H> instanceFor(H kind) {
            return switch (kind) {
                case HashKind.K128 _ -> (Null<K, H>) NULL_K128;
                case HashKind.K256 _ -> (Null<K, H>) NULL_K256;
            };
        }

        @Override
        public Hash<K> hash() {
            return kind.blank();
        }

        /// @return null
        @Override
        public Object unwrap() {
            return null;
        }

        private static final Null<?, ?> NULL_K128 = new Null<>(HashKind.K128);

        private static final Null<?, ?> NULL_K256 = new Null<>(HashKind.K256);
    }
}
