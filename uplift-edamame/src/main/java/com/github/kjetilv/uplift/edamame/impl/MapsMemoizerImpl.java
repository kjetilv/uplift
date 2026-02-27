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
/// @param <MK> Key type for the maps. All maps (and their submaps) will be stored with keys of this type
/// @param <K> Hash kind
@SuppressWarnings("unchecked")
class MapsMemoizerImpl<I, MK, K extends HashKind<K>>
    implements MapsMemoizer<I, MK> {

    private final Map<I, Hash<K>> hashes = new HashMap<>();

    private final Map<Hash<K>, Map<MK, Object>> objects = new HashMap<>();

    private final Map<I, Map<MK, Object>> overflow = new HashMap<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final TreeHasher<MK, K> treeHasher;

    private final Canonicalizer<MK, K> canonicalValues;

    /// @param canonicalValues Not null
    /// @see MapsMemoizers#create(com.github.kjetilv.uplift.edamame.KeyHandler, HashKind)
    MapsMemoizerImpl(TreeHasher<MK, K> treeHasher, Canonicalizer<MK, K> canonicalValues) {
        this.treeHasher = requireNonNull(treeHasher, "mapHasher");
        this.canonicalValues = requireNonNull(canonicalValues, "canonicalValues");
    }

    @Override
    public int size() {
        return withReadLock(() -> hashes.size() + overflow.size());
    }

    @Override
    public Map<MK, ?> get(I id) {
        requireNonNull(id, "id");
        return withReadLock(() -> {
            var hash = hashes.get(id);
            return hash == null ? overflow.get(id) : objects.get(hash);
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
    public MemoizedMaps<I, MK> maps(boolean copy) {
        return withReadLock(() ->
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
            case CanonicalValue.Node<?, K>(Hash<K> hash, Map<?, Object> node) -> withWriteLock(() ->
                putCanonical(identifier, hash, (Map<MK, Object>) node, requireAbsent)
            );
            case CanonicalValue.Collision<K> _ -> withWriteLock(() ->
                putOverflow(identifier, (Map<MK, Object>) value)
            );
            case CanonicalValue<K> other -> fail("Unexpected canonical value: " + other);
        };
    }

    private boolean putCanonical(
        I identifier,
        Hash<K> hash,
        Map<MK, Object> value,
        boolean requireAbsent
    ) {
        return withWriteLock(() -> {
            var existingHash = hashes.putIfAbsent(identifier, hash);
            if (existingHash == null) {
                var existingValue = objects.putIfAbsent(hash, value);
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

    private boolean putOverflow(I identifier, Map<MK, Object> value) {
        return withWriteLock(() ->
            overflow.putIfAbsent(identifier, value) == null);
    }

    private <T> T withReadLock(Supplier<T> action) {
        return withLock(lock.readLock(), action);
    }

    private <T> T withWriteLock(Supplier<T> action) {
        return withLock(lock.writeLock(), action);
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
        return getClass().getSimpleName() + "[" + withReadLock(this::describe) + "]";
    }
}
