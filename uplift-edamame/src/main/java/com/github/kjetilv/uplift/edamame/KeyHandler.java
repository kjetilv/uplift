package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.util.Bytes;

/// Strategy interface for key handling.  [Normalizes][#normalize(Object)] arbitrary objects into keys,
/// and provides a way to [byte-encode][#bytes(Object)] keys for hashing purposes. This allows key types which
/// are non-stringly-typed, such as single-field {@link Record records}.
///
/// In such cases, [implement][#normalize(Object)] this interface and
/// [plug it in][MapsMemoizers#create(KeyHandler,com.github.kjetilv.uplift.hash.HashKind)]
/// to produce instances of that key type.
///
/// However, often maps have [string][String] keys, or at least some key type with a suitable
/// [toString()][Object#toString()], like {@link Enum enums}. In this case, use
/// [the default memoizer][MapsMemoizers#create(com.github.kjetilv.uplift.hash.HashKind)]
/// which simply normalizes to strings.
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
        return bytes(key);
    }

    /// Affects how maps are hashed wrt. their keys.  Default implementation is to get the
    /// bytes of its [toString][Object#toString()].
    ///
    /// @param key Key
    /// @return byte array for hashing
    default Bytes bytes(K key) {
        return Bytes.from(key.toString());
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
