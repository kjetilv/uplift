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
import static com.github.kjetilv.uplift.util.Collectioons.*;
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

    private Hash<H> hashMap(Map<I, ? extends HashedTree<I, H>> tree) {
        return StructureHasher.hashMap(newBuilder.get(), tree, keyHandler);
    }

    private Hash<H> hashList(List<? extends HashedTree<I, H>> trees) {
        return StructureHasher.hashList(newBuilder.get(), trees);
    }

    private Hash<H> hashLeaf(Object value) {
        return leafHasher.hash(value);
    }

    /// Normalizes keys and hashes values. Entries with empty values are left out.
    ///
    /// @throws IllegalArgumentException If two keys normalize to the same key
    private static <MK, K extends HashKind<K>> Map<MK, HashedTree<MK, K>> transformMap(
        Map<?, ?> value,
        KeyHandler<MK> keyHandler,
        Function<Object, HashedTree<MK, K>> transform
    ) {
        Map<MK, HashedTree<MK, K>> transformed = HashMap.newHashMap(value.size());
        value.forEach((key, val) -> {
            if (!isEmpty(val)) {
                var normalized = keyHandler.normalize(key);
                if (transformed.putIfAbsent(normalized, transform.apply(val)) != null) {
                    throw duplicateKey(value, key, normalized, keyHandler);
                }
            }
        });
        return Collections.unmodifiableMap(transformed);
    }

    private static IllegalArgumentException duplicateKey(
        Map<?, ?> value,
        Object key,
        Object normalized,
        KeyHandler<?> keyHandler
    ) {
        var otherKey = value.keySet()
            .stream()
            .filter(other -> other != key && !isEmpty(value.get(other)))
            .filter(other -> normalized.equals(keyHandler.normalize(other)))
            .findFirst()
            .map(RecursiveTreeHasher::describe)
            .orElse("?");
        return new IllegalArgumentException(
            "Keys " + otherKey + " and " + describe(key) + " both normalize to " + describe(normalized));
    }

    private static String describe(Object key) {
        return key + " (" + key.getClass().getSimpleName() + ")";
    }

    private static boolean isEmpty(Object value) {
        return value != null && switch (value) {
            case Map<?, ?> map -> map.isEmpty();
            case Iterable<?> iterable -> !iterable.iterator().hasNext();
            case Object object -> isEmptyArray(object);
        };
    }
}
