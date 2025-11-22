package com.github.kjetilv.uplift.edamame.impl;

import module java.base;
import com.github.kjetilv.uplift.edamame.*;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import static java.util.Collections.unmodifiableMap;
import static java.util.Map.copyOf;
import static java.util.Objects.requireNonNull;

/// Works by hashing nodes and leaves and storing them under their hashes. When structures and/or values
/// re-occur, they are replaced by the already registered, canonical instances.
///
/// If an incoming value provokes a hash collision, it will be stored as-is and separately from the canonical
/// trees.  This should be rare, especially with [SHA3-256][com.github.kjetilv.uplift.hash.HashKind.K256].
///
/// Use [MapsMemoizers#create(HashKind)] and siblings to create instances of this class.
///
/// @param <I> Identifier type.  An identifier identifies exactly one of the cached maps
/// @param <K> Key type for the maps. All maps (and their submaps) will be stored with keys of this type
/// @param <H> Hash kind
@SuppressWarnings("unchecked")
class MapsMemoizerImpl<I, K, H extends HashKind<H>>
    implements MapsMemoizer<I, K> {

    private final Map<I, Hash<H>> hashes = new HashMap<>();

    private final Map<Hash<H>, Map<K, Object>> objects = new HashMap<>();

    private final Map<I, Map<K, Object>> overflow = new HashMap<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final TreeHasher<K, H> treeHasher;

    private final Canonicalizer<K, H> canonicalValues;

    /// @param canonicalValues Not null
    /// @see MapsMemoizers#create(com.github.kjetilv.uplift.edamame.KeyHandler, HashKind)
    MapsMemoizerImpl(TreeHasher<K, H> treeHasher, Canonicalizer<K, H> canonicalValues) {
        this.treeHasher = requireNonNull(treeHasher, "mapHasher");
        this.canonicalValues = requireNonNull(canonicalValues, "canonicalValues");
    }

    @Override
    public int size() {
        return read(() -> hashes.size() + overflow.size());
    }

    @Override
    public Map<K, ?> get(I id) {
        requireNonNull(id, "id");
        return read(() -> {
            var hash = hashes.get(id);
            return hash != null ? objects.get(hash)
                : overflow.get(id);
        });
    }

    @Override
    public void put(I identifier, Map<?, ?> value) {
        put(identifier, value, true);
    }

    @Override
    public boolean putIfAbsent(I identifier, Map<?, ?> value) {
        return put(identifier, value, false);
    }

    @Override
    public MemoizedMaps<I, K> maps(boolean copy) {
        return read(() ->
            new MemoizedMapsImpl<>(
                copy ? copyOf(hashes) : unmodifiableMap(hashes),
                copy ? copyOf(objects) : unmodifiableMap(objects),
                overflow.isEmpty() ? Map.of()
                    : copy ? copyOf(overflow)
                        : unmodifiableMap(overflow)
            ));
    }

    @SuppressWarnings({"unused"})
    private boolean put(I identifier, Map<?, ?> value, boolean requireAbsent) {
        requireNonNull(identifier, "identifier");
        requireNonNull(value, "value");
        var hashedTree = treeHasher.tree(value);
        var canonical = canonicalValues.canonical(hashedTree);
        return switch (canonical) {
            case CanonicalValue.Node<?, H> node -> write(() ->
                putCanonical(identifier, node, requireAbsent)
            );
            case CanonicalValue.Collision<H> _ -> write(() ->
                putOverflow(identifier, (Map<K, Object>) value)
            );
            case CanonicalValue<H> other -> fail("Unexpected canonical value: " + other);
        };
    }

    private boolean putCanonical(I identifier, CanonicalValue.Node<?, H> valueNode, boolean requireAbsent) {
        return write(() -> {
            var existingHash = hashes.putIfAbsent(identifier, valueNode.hash());
            if (existingHash == null) {
                var value = (Map<K, Object>) valueNode.value();
                var existingValue = objects.putIfAbsent(valueNode.hash(), value);
                if (existingValue == null || existingValue.equals(value)) {
                    return true;
                }
                return fail("Illegal state: " + existingValue + " != " + value);
            }
            if (requireAbsent) {
                throw new IllegalArgumentException("Identifier " + identifier + " was: " + get(identifier));
            }
            return false;
        });
    }

    private boolean putOverflow(I identifier, Map<K, Object> value) {
        return write(() ->
            overflow.putIfAbsent(identifier, value) == null);
    }

    private <T> T read(Supplier<T> action) {
        return withLock(lock.readLock(), action);
    }

    private <T> T write(Supplier<T> action) {
        return withLock(lock.writeLock(), action);
    }

    private String describe() {
        var count = hashes.size();
        var canonicalsSize = objects.size();
        int overflowsCount = overflow.size();
        return (count + overflowsCount) +
               " items" +
               (overflowsCount == 0 ? ", " : " (" + overflowsCount + " collisions), ") +
               "working maps:" + canonicalsSize;
    }

    private static <T> T withLock(
        Lock lock,
        Supplier<T> action
    ) {
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    private static <T> T fail(String other) {
        throw new IllegalStateException(other);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + read(this::describe) + "]";
    }
}
