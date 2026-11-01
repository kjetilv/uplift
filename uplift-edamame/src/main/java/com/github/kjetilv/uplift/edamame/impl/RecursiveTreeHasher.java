package com.github.kjetilv.uplift.edamame.impl;

import module java.base;
import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.edamame.MapsMemoizers;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.util.Bytes;

import static com.github.kjetilv.uplift.edamame.HashedTree.*;
import static com.github.kjetilv.uplift.edamame.impl.Tag.LIST;
import static com.github.kjetilv.uplift.edamame.impl.Tag.MAP;
import static com.github.kjetilv.uplift.util.Collectioons.*;
import static com.github.kjetilv.uplift.util.Maps.sizedMap;
import static java.util.Objects.requireNonNull;

/// Normalizes input trees and builds [hashed trees][HashedTree]. Stateless and thread-safe.
///
/// @param <I> Identifier type
public record RecursiveTreeHasher<I, H extends HashKind<H>>(
    Supplier<HashBuilder<Bytes, H>> newBuilder,
    KeyHandler<I> keyHandler,
    LeafHasher<H> leafHasher,
    H kind
) implements TreeHasher<I, H> {

    /// @param newBuilder Hash builder, not null
    /// @param keyHandler Key handler, not null
    /// @param leafHasher Hasher, not null
    /// @see MapsMemoizers#createWith(KeyHandler, HashKind)
    public RecursiveTreeHasher(
        Supplier<HashBuilder<Bytes, H>> newBuilder,
        KeyHandler<I> keyHandler,
        LeafHasher<H> leafHasher,
        H kind
    ) {
        this.newBuilder = requireNonNull(newBuilder, "newBuilder");
        this.keyHandler = requireNonNull(keyHandler, "keyHandler");
        this.leafHasher = requireNonNull(leafHasher, "leafHasher");
        this.kind = requireNonNull(kind, "kind");
    }

    @Override
    public HashedTree<I, H> tree(Object value) {
        return switch (value) {
            case Map<?, ?> map -> {
                var hashedMap = transformMap(map, keyHandler, this::tree);
                var hash = hashMap(hashedMap);
                yield new Node<>(hash, hashedMap);
            }
            case Iterable<?> iterable -> {
                var hashedValues = transformList(iterable, this::tree);
                var hash = hashList(hashedValues);
                yield new Nodes<>(hash, hashedValues);
            }
            case null -> Null.instanceFor(kind);
            default -> {
                if (value.getClass().isArray()) {
                    var hashedValues = transformList(iterable(value), this::tree);
                    var hash = hashList(hashedValues);
                    yield new Nodes<>(hash, hashedValues);
                }
                yield new Leaf<>(hashLeaf(value), value);
            }
        };
    }

    /// Hashes `MAP` tag, size, then for each entry: key length, key bytes and value hash.
    ///
    /// The tag separates maps from lists and leaves. The key length makes the encoding unambiguous.
    private Hash<H> hashMap(Map<I, ? extends HashedTree<I, H>> tree) {
        var hb = MAP.tag(newBuilder.get());
        HashBuilder<Hash<H>, H> hashHb = hb.map(Hash::toBytes);
        hb.hash(Bytes.intBytes(tree.size()));
        tree.forEach((key, value) -> {
            var keyBytes = keyHandler.bytes(key);
            hb.hash(Bytes.intBytes(keyBytes.length()));
            hb.hash(keyBytes);
            hashHb.hash(value.hash());
        });
        return hb.build();
    }

    /// Hashes `LIST` tag, size, then the hash of each element.
    private Hash<H> hashList(List<? extends HashedTree<I, H>> trees) {
        var hb = LIST.tag(newBuilder.get());
        HashBuilder<Hash<H>, H> hashHb = hb.map(Hash::toBytes);
        hb.hash(Bytes.intBytes(trees.size()));
        trees.stream()
            .map(HashedTree::hash)
            .forEach(hashHb::hash);
        return hb.build();
    }

    private Hash<H> hashLeaf(Object value) {
        return leafHasher.hash(value);
    }

    private static <MK, K extends HashKind<K>> Map<MK, HashedTree<MK, K>> transformMap(
        Map<?, ?> value,
        KeyHandler<MK> keyHandler,
        Function<Object, HashedTree<MK, K>> transform
    ) {
        return sizedMap(value.entrySet()
            .stream()
            .filter(empty().negate())
            .collect(Collectors.toMap(
                entry -> keyHandler.normalize(entry.getKey()),
                entry -> transform.apply(entry.getValue())
            )));
    }

    private static Predicate<Map.Entry<?, ?>> empty() {
        return entry -> isEmpty(entry.getValue());
    }

    private static boolean isEmpty(Object value) {
        return value != null && switch (value) {
            case Map<?, ?> map -> map.isEmpty();
            case Iterable<?> iterable -> !iterable.iterator().hasNext();
            case Object object -> isEmptyArray(object);
        };
    }
}
