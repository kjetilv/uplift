package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.util.Bytes;

/// Strategy interface for key handling.  [Normalizes][#normalize(Object)] arbitrary objects into keys,
/// and provides a way to [byte-encode][#bytes(Object)] keys for hashing purposes.
///
/// Maps will likely have [string][String] keys, or at least some key type which has a
/// [natural projection][Object#toString()] onto strings – this is fancy for "implements toString". In this case,
/// use [the default memoizer][LeafHasher#create(com.github.kjetilv.uplift.hash.HashKind)].
///
/// Rationale: You may be the type to avoid stringly typed code, using e.g. a single-value
/// [Record], or an [Enum]. In such cases, [implement][#normalize(Object)] this interface and
/// [plug it in][MapsMemoizers#create(KeyHandler,com.github.kjetilv.uplift.hash.HashKind)] to produce instances of that key type.
///
/// @param <K> Key type
/// @see MapsMemoizers#create(KeyHandler, com.github.kjetilv.uplift.hash.HashKind)
@FunctionalInterface
public interface KeyHandler<K> {

    @SuppressWarnings("unchecked")
    static <K> KeyHandler<K> defaultHandler() {
        return key -> (K) key.toString();
    }

    default Bytes toBytes(K key) {
        return Bytes.from(bytes(key));
    }

    /// Affects how maps are hashed wrt. their keys.  Default implementation is to get the
    /// bytes of its [toString][Object#toString()].
    ///
    /// @param key Key
    /// @return byte array for hashing
    default byte[] bytes(K key) {
        return key.toString().getBytes();
    }

    /// Normalizes a map's key to a K instance.  The returned value will be canonicalized so that the same key
    /// gets the same `K` instance.
    ///
    /// Note that this method must accept [any][Object] value. The [MapsMemoizer] needs to work
    /// on `Map<?, ?>`, so it is up to the handler to resolve maps' keys into [K]'s.
    ///
    /// @param key Key
    /// @return A K instance
    K normalize(Object key);
}
