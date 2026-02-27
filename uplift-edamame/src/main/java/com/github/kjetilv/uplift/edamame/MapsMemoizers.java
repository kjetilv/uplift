package com.github.kjetilv.uplift.edamame;

import module java.base;
import com.github.kjetilv.uplift.edamame.impl.InternalFactory;
import com.github.kjetilv.uplift.hash.HashKind;

/// Factory methods for [MapsMemoizer]s.
public final class MapsMemoizers {

    /// This method returns a memoizer using strings for keys, via regular [toString][#toString()]
    ///
    /// @param <I> Top-level id type
    /// @return [MapsMemoizer] for String-keyed maps
    public static <I, K extends HashKind<K>> MapsMemoizer<I, String> create(K kind) {
        return create(null, null, kind);
    }

    /// This method affords control over stored keys. Stored maps will be normalized to use [MK]'s
    /// as map keys, on all levels. The `keyHandler` argument provides a callback that will
    /// produce [MK] instances from keys in incoming maps.
    ///
    /// Since [MapsMemoizer] accepts [Map<?, ?>][Map], this function needs to accept any
    /// input, i.e. [?][Object].
    ///
    /// @param <I>        Id type
    /// @param <MK>        Key type
    /// @param keyHandler Key handler
    /// @return Map memoizer
    public static <I, MK, K extends HashKind<K>> MapsMemoizer<I, MK> create(
        KeyHandler<MK> keyHandler,
        K kind
    ) {
        return InternalFactory.create(keyHandler, kind);
    }

    public static <I, MK, K extends HashKind<K>> MapsMemoizer<I, MK> create(
        PojoBytes pojoBytes,
        K kind
    ) {
        return create(null, pojoBytes, kind);
    }

    public static <I, MK, K extends HashKind<K>> MapsMemoizer<I, MK> create(
        KeyHandler<MK> keyHandler,
        PojoBytes pojoBytes,
        K kind
    ) {
        return InternalFactory.create(keyHandler, kind, pojoBytes);
    }

    private MapsMemoizers() {
    }
}
