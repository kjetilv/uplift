package com.github.kjetilv.uplift.edamame.impl;

import module java.base;
import com.github.kjetilv.uplift.edamame.CanonicalValue;
import com.github.kjetilv.uplift.edamame.CanonicalValue.Collision;
import com.github.kjetilv.uplift.edamame.Canonicalizer;
import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.HashedTree.Node;
import com.github.kjetilv.uplift.edamame.HashedTree.Nodes;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import static com.github.kjetilv.uplift.util.Collectioons.transformList;
import static com.github.kjetilv.uplift.util.Maps.transformMap;

/// Canonicalizes [hashed trees][HashedTree], progressively storing and resolving shared substructures
/// as they appear.
///
/// This class ought to be thread-safe, as it only appends to [concurrent maps][ConcurrentMap].
///
/// @param <K>
public final class CanonicalSubstructuresCataloguer<K, H extends HashKind<H>>
    implements Canonicalizer<K, H> {

    public static <MK, K extends HashKind<K>> Canonicalizer<MK, K> create() {
        return new CanonicalSubstructuresCataloguer<>();
    }

    private final Map<Hash<H>, Map<K, Object>> maps = new ConcurrentHashMap<>();

    private final Map<Hash<H>, List<Object>> lists = new ConcurrentHashMap<>();

    private final Map<Hash<H>, Object> leaves = new ConcurrentHashMap<>();

    private final boolean noCollisions;

    CanonicalSubstructuresCataloguer(boolean noCollisions) {
        this.noCollisions = noCollisions;
    }

    private CanonicalSubstructuresCataloguer() {
        this(false);
    }

    @Override
    public CanonicalValue<H> canonical(HashedTree<K, H> hashedTree) {
        return switch (hashedTree) {
            case Node<K, H>(var hash, var map) -> {
                var known = maps.get(hash);
                if (known != null && (noCollisions || sameAs(known, hashedTree))) {
                    yield new CanonicalValue.Node<>(hash, known);
                }
                var node = transformMap(map, this::canonical);
                var collision = noCollisions ? null : collision(node.values());
                yield noCollisions || collision == null ? canonicalize(
                    compact(node),
                    hash,
                    maps,
                    t -> new CanonicalValue.Node<>(hash, t)
                ) : collision;
            }
            case Nodes<K, H>(var hash, var list) -> {
                var known = lists.get(hash);
                if (known != null && (noCollisions || sameAs(known, hashedTree))) {
                    yield new CanonicalValue.Nodes<>(hash, known);
                }
                var nodes = transformList(list, this::canonical);
                var collision = noCollisions ? null : collision(nodes);
                yield noCollisions || collision == null ? canonicalize(
                    transformList(nodes, CanonicalValue::value),
                    hash,
                    lists,
                    t -> new CanonicalValue.Nodes<>(hash, t)
                ) : collision;
            }
            case HashedTree.Leaf<?, H>(var hash, var value) -> {
                var known = leaves.get(hash);
                yield known != null && (noCollisions || known.equals(value))
                    ? new CanonicalValue.Leaf<>(hash, known)
                    : canonicalize(value, hash, leaves, t -> new CanonicalValue.Leaf<>(hash, t));
            }
            case HashedTree.Null<?, H>(var kind) -> CanonicalValue.Null.instanceFor(kind);
        };
    }

    /// Stores a map in compact form, without one entry object per entry, unless it holds a null value, which
    /// the compact form does not allow.
    ///
    /// @param node Canonical values by key
    /// @return Immutable map of the values
    @SuppressWarnings("unchecked")
    private static <K, H extends HashKind<H>> Map<K, Object> compact(Map<K, CanonicalValue<H>> node) {
        var entries = new Map.Entry[node.size()];
        var i = 0;
        for (var entry : node.entrySet()) {
            var value = entry.getValue().value();
            if (value == null) {
                return transformMap(node, CanonicalValue::value);
            }
            entries[i++] = Map.entry(entry.getKey(), value);
        }
        return Map.ofEntries(entries);
    }

    /// Compares a known canonical value with a hashed tree, without building anything. Used to confirm that a
    /// tree with a known hash is equal to the known value, and not a hash collision.
    ///
    /// @param canonical Known canonical value: map, list, leaf or null
    /// @param tree      Hashed tree
    /// @return true iff equal
    private boolean sameAs(Object canonical, HashedTree<K, H> tree) {
        return switch (tree) {
            case Node<K, H>(_, var map) ->
                canonical instanceof Map<?, ?> known && known.size() == map.size() && sameEntries(known, map);
            case Nodes<K, H>(_, var list) ->
                canonical instanceof List<?> known && known.size() == list.size() && sameElements(known, list);
            case HashedTree.Leaf<?, H>(_, var value) -> value.equals(canonical);
            case HashedTree.Null<?, H> _ -> canonical == null;
        };
    }

    private boolean sameEntries(Map<?, ?> known, Map<K, HashedTree<K, H>> map) {
        for (var entry : map.entrySet()) {
            var value = known.get(entry.getKey());
            if (value == null && !known.containsKey(entry.getKey()) || !sameAs(value, entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private boolean sameElements(List<?> known, List<HashedTree<K, H>> list) {
        for (var i = 0; i < list.size(); i++) {
            if (!sameAs(known.get(i), list.get(i))) {
                return false;
            }
        }
        return true;
    }

    private Collision<H> collision(Iterable<CanonicalValue<H>> values) {
        for (var value : values) {
            if (value instanceof Collision<H> collision) {
                return collision;
            }
        }
        return null;
    }

    private <T> CanonicalValue<H> canonicalize(
        T value,
        Hash<H> hash,
        Map<Hash<H>, T> map,
        Function<T, CanonicalValue<H>> wrap
    ) {
        var existing = map.putIfAbsent(hash, value);
        return existing == null ? wrap.apply(value)
            : noCollisions || existing.equals(value) ? wrap.apply(existing)
                : new Collision<>(hash, value);
    }
}
