package com.github.kjetilv.uplift.edamame;

import module java.base;
import com.github.kjetilv.uplift.edamame.impl.StructureHasher;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.util.Bytes;

/// Hashes for [nodes][HashedTree.Node] and [lists][HashedTree.Nodes], computed from the hashes of their parts.
///
/// Use these when building [hashed trees][HashedTree] outside this library, so that the hashes match those of
/// [MapsMemoizer] and are safe for [canonicalizers][Canonicalizer]:
/// * Maps, lists and leaves never share a hash.
/// * Map hashes do not depend on iteration order.
public final class StructureHashes {

    /// @param builder    New hash builder
    /// @param map        Map of hashed trees, as it will be stored
    /// @param keyHandler Provides key bytes
    /// @return Hash of the map
    public static <K, H extends HashKind<H>> Hash<H> map(
        HashBuilder<Bytes, H> builder,
        Map<K, ? extends HashedTree<K, H>> map,
        KeyHandler<K> keyHandler
    ) {
        return StructureHasher.hashMap(builder, map, keyHandler);
    }

    /// @param builder New hash builder
    /// @param list    List of hashed trees, as it will be stored
    /// @return Hash of the list
    public static <H extends HashKind<H>> Hash<H> list(
        HashBuilder<Bytes, H> builder,
        List<? extends HashedTree<?, H>> list
    ) {
        return StructureHasher.hashList(builder, list);
    }

    private StructureHashes() {
    }
}
