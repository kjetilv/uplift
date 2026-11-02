package com.github.kjetilv.uplift.edamame.impl;

import module java.base;
import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.edamame.StructureHashes;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.util.Bytes;

import static com.github.kjetilv.uplift.edamame.impl.Tag.LIST;
import static com.github.kjetilv.uplift.edamame.impl.Tag.MAP;

/// Hashes maps and lists from the hashes of their parts. Use via [StructureHashes].
public final class StructureHasher {

    /// Hashes `MAP` tag, size, then for each entry: key length, key bytes and value hash.
    ///
    /// The tag separates maps from lists and leaves. The key length makes the encoding unambiguous.
    /// Entries are hashed in [key bytes order][HashedEntry#ORDER], so the hash does not depend on the
    /// iteration order of the map.
    ///
    /// @param builder    New hash builder
    /// @param map        Map of hashed trees
    /// @param keyHandler Provides key bytes
    /// @return Hash of the map
    public static <K, H extends HashKind<H>> Hash<H> hashMap(
        HashBuilder<Bytes, H> builder,
        Map<K, ? extends HashedTree<K, H>> map,
        KeyHandler<K> keyHandler
    ) {
        var hb = MAP.tag(builder);
        HashBuilder<Hash<H>, H> hashHb = hb.map(Hash::toBytes);
        hb.hash(Bytes.intBytes(map.size()));
        map.entrySet()
            .stream()
            .map(entry ->
                new HashedEntry<>(keyHandler.bytes(entry.getKey()), entry.getValue().hash()))
            .sorted(HashedEntry.order())
            .forEachOrdered(entry -> {
                hb.hash(Bytes.intBytes(entry.keyBytes().length()));
                hb.hash(entry.keyBytes());
                hashHb.hash(entry.valueHash());
            });
        return hb.build();
    }

    /// Hashes `LIST` tag, size, then the hash of each element.
    ///
    /// @param builder New hash builder
    /// @param list    List of hashed trees
    /// @return Hash of the list
    public static <H extends HashKind<H>> Hash<H> hashList(
        HashBuilder<Bytes, H> builder,
        List<? extends HashedTree<?, H>> list
    ) {
        var hb = LIST.tag(builder);
        HashBuilder<Hash<H>, H> hashHb = hb.map(Hash::toBytes);
        hb.hash(Bytes.intBytes(list.size()));
        list.forEach(tree -> hashHb.hash(tree.hash()));
        return hb.build();
    }

    private StructureHasher() {
    }

    /// Map entry as hashed: key bytes and value hash.
    private record HashedEntry<H extends HashKind<H>>(Bytes keyBytes, Hash<H> valueHash) {

        /// Orders by key bytes, unsigned. Two keys may have the same bytes if a [KeyHandler] maps them so;
        /// then the value hash decides.
        @SuppressWarnings({"unchecked", "rawtypes"})
        static <H extends HashKind<H>> Comparator<HashedEntry<H>> order() {
            return (Comparator) ORDER;
        }

        private static final Comparator<HashedEntry<?>> ORDER = (one, other) -> {
            var keyOrder = Arrays.compareUnsigned(
                one.keyBytes().bytes(), one.keyBytes().offset(), one.keyBytes().offset() + one.keyBytes().length(),
                other.keyBytes().bytes(), other.keyBytes().offset(), other.keyBytes().offset() + other.keyBytes().length()
            );
            return keyOrder != 0 ? keyOrder : Arrays.compareUnsigned(one.valueHash().bytes(), other.valueHash().bytes());
        };
    }
}
