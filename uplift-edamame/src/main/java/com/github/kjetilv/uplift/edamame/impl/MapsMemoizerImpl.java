package com.github.kjetilv.uplift.edamame.impl;

import module java.base;
import com.github.kjetilv.uplift.edamame.*;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

/// Works by hashing nodes and leaves and storing them under their hashes. When structures and/or values
/// re-occur, they are replaced by the already registered, canonical instances.
///
/// If an incoming value provokes a hash collision, it will be stored as-is and separately from the canonical
/// trees.  This should be rare, especially with [SHA3-256][com.github.kjetilv.uplift.hash.HashKind.K256].
///
/// Use [MapsMemoizers#create(HashKind)] and siblings to create instances of this class.
///
/// @param <I>  Identifier type.  An identifier identifies exactly one of the cached maps
/// @param <K> Key type for the maps. All maps (and their submaps) will be stored with keys of this type
/// @param <H>  Hash kind
@SuppressWarnings("unchecked")
class MapsMemoizerImpl<I, K, H extends HashKind<H>>
    implements MapsMemoizer<I, K> {

    // Concurrent maps: reads take no lock. Writes are serialized by the lock, and store the value before the
    // identifier, so a reader that finds an identifier also finds its value.

    private final Map<I, Hash<H>> hashes = new ConcurrentHashMap<>();

    private final Map<Hash<H>, Map<K, Object>> objects = new ConcurrentHashMap<>();

    private final Map<I, Map<K, Object>> overflow = new ConcurrentHashMap<>();

    private final Lock lock = new ReentrantLock();

    private final TreeHasher<K, H> treeHasher;

    private final Canonicalizer<K, H> canonicalValues;

    /// @param canonicalValues Not null
    /// @see MapsMemoizers#createWith(com.github.kjetilv.uplift.edamame.KeyHandler, HashKind)
    MapsMemoizerImpl(TreeHasher<K, H> treeHasher, Canonicalizer<K, H> canonicalValues) {
        this.treeHasher = requireNonNull(treeHasher, "mapHasher");
        this.canonicalValues = requireNonNull(canonicalValues, "canonicalValues");
    }

    @Override
    public int size() {
        return hashes.size() + overflow.size();
    }

    @Override
    public Map<K, ?> get(I id) {
        requireNonNull(id, "id");
        var hash = hashes.get(id);
        return hash == null ? overflow.get(id) : objects.get(hash);
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
        return copy
            ? withWriteLock(this::flatCopy)
            : new MemoizedMapsImpl<>(unmodifiableMap(hashes), unmodifiableMap(objects), unmodifiableMap(overflow));
    }

    /// Call with the write lock held.
    ///
    /// @return Copy with one lookup per identifier, which does not retain the hashes
    private MemoizedMaps<I, K> flatCopy() {
        Map<I, Map<K, ?>> flat = HashMap.newHashMap(hashes.size() + overflow.size());
        hashes.forEach((id, hash) -> flat.put(id, objects.get(hash)));
        flat.putAll(overflow);
        return new FlatMemoizedMaps<>(Map.copyOf(flat));
    }

    @SuppressWarnings({"unused"})
    private boolean put(I identifier, Map<?, ?> value, boolean requireAbsent) {
        requireNonNull(identifier, "identifier");
        requireNonNull(value, "value");
        var hashedTree = treeHasher.tree(value);
        var canonical = canonicalValues.canonical(hashedTree);
        return switch (canonical) {
            case CanonicalValue.Node<?, H>(
                var hash,
                var node
            ) -> withWriteLock(() ->
                putIfNew(
                    identifier,
                    requireAbsent,
                    () ->
                        putCanonical(identifier, hash, (Map<K, Object>) node)
                )
            );
            case CanonicalValue.Collision<H> _ -> {
                var normalized = (Map<K, Object>) hashedTree.unwrap();
                yield withWriteLock(() ->
                    putIfNew(
                        identifier,
                        requireAbsent,
                        () ->
                            overflow.put(identifier, normalized)
                    )
                );
            }
            case CanonicalValue<H> other -> fail("Unexpected canonical value: " + other);
        };
    }

    /// Stores the value if the identifier is not known, in either the canonical or the overflow store.
    /// Call with the lock held.
    ///
    /// @param identifier    Identifier
    /// @param requireAbsent If true, a known identifier is an error
    /// @param store         Stores the value
    /// @return true iff the value was stored
    /// @throws IllegalArgumentException If requireAbsent is true and the identifier is known
    private boolean putIfNew(I identifier, boolean requireAbsent, Runnable store) {
        if (hashes.containsKey(identifier) || overflow.containsKey(identifier)) {
            if (requireAbsent) {
                throw new IllegalArgumentException("Identifier " + identifier + " was: " + get(identifier));
            }
            return false;
        }
        store.run();
        return true;
    }

    /// Call with the lock held. The identifier is registered only after the value is accepted.
    private void putCanonical(I identifier, Hash<H> hash, Map<K, Object> value) {
        var existingValue = objects.putIfAbsent(hash, value);
        if (existingValue != null && !existingValue.equals(value)) {
            fail("Illegal state: " + existingValue + " != " + value);
        }
        hashes.put(identifier, hash);
    }

    private <T> T withWriteLock(Supplier<T> action) {
        return withLock(lock, action);
    }

    private String describe() {
        var count = hashes.size();
        var canonicalsSize = objects.size();
        var overflowsCount = overflow.size();
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
        return getClass().getSimpleName() + "[" + describe() + "]";
    }
}
