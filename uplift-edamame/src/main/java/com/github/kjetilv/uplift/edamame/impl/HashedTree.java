package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.List;
import java.util.Map;

/**
 * A hashed tree mirrors a structure we want to store, decorating each part of the tree with a unique
 * {@link #hash() hash}.
 *
 * @param <T> Type of contents
 */
sealed interface HashedTree<T, H extends HashKind<H>> {

    /**
     * @return The hash of this part of the tree
     */
    Hash<H> hash();

    /**
     * Get the original structure back
     *
     * @return Original structure
     */
    T unwrap();

    /**
     * A node in the tree
     *
     * @param hash     Hash
     * @param valueMap Map
     * @param <S>      Type of key in the map
     */
    record Node<S, H extends HashKind<H>>(
        Hash<H> hash,
        Map<S, ? extends HashedTree<?, H>> valueMap
    ) implements HashedTree<Map<S, Object>, H> {

        @Override
        public Map<S, Object> unwrap() {
            return CollectionUtils.transformValues(
                valueMap,
                HashedTree::unwrap
            );
        }
    }

    /**
     * A list in the tree
     *
     * @param hash   Hash
     * @param values List
     */
    record Nodes<H extends HashKind<H>>(
        Hash<H> hash,
        List<? extends HashedTree<?, H>> values
    )
        implements HashedTree<List<? extends HashedTree<?, H>>, H> {

        @SuppressWarnings({"unchecked", "ClassEscapesDefinedScope"})
        @Override
        public List<? extends HashedTree<?, H>> unwrap() {
            return (List<? extends HashedTree<?, H>>) CollectionUtils.transform(
                values,
                HashedTree::unwrap
            );
        }
    }

    /**
     * A leaf in the tree
     *
     * @param hash  Hash
     * @param value Leaf
     */
    record Leaf<H extends HashKind<H>>(Hash<H> hash, Object value)
        implements HashedTree<Object, H> {

        @Override
        public Object unwrap() {
            return value;
        }
    }

    /**
     * Null value, which may occur in a list. Has the {@link HashKind#blank() null} hash.
     */
    record Null<H extends HashKind<H>>(Hash<H> hash)
        implements HashedTree<Void, H> {

        @Override
        public Void unwrap() {
            return null;
        }
    }
}
