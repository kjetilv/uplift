package com.github.kjetilv.uplift.edamame;

import module java.base;
import module uplift.hash;
import com.github.kjetilv.uplift.edamame.impl.InternalFactory;

/// Factory methods for [MapsMemoizer]s.
public final class MapsMemoizers {

    /// strings via regular [toString][#toString()]
    ///
    /// @param <I> Type of id's
    /// @return [MapsMemoizer] for String-keyed maps
    public static <I, H extends HashKind<H>> MapsMemoizer<I, String> create(H kind) {
        return create(null, null, kind);
    }

    /// This method affords control over stored keys. Stored maps will be normalized to use [K]'s
    /// as map keys, on all levels. The `keyHandler` argument provides a callback that will
    /// produce [K] instances from keys in incoming maps.
    ///
    /// Since [MapsMemoizer] accepts [Map<?, ?>][Map], this function needs to accept any
    /// input, i.e. [?][Object].
    ///
    /// @param <I>        Id type
    /// @param <K>        Key type
    /// @param keyHandler Key handler
    /// @return Map memoizer
    public static <I, K, H extends HashKind<H>> MapsMemoizer<I, K> create(
        KeyHandler<K> keyHandler,
        H kind
    ) {
        return InternalFactory.create(keyHandler, kind);
    }

    public static <I, K, H extends HashKind<H>> MapsMemoizer<I, K> create(
        PojoBytes pojoBytes,
        H kind
    ) {
        return create(null, pojoBytes, kind);
    }

    public static <I, K, H extends HashKind<H>> MapsMemoizer<I, K> create(
        KeyHandler<K> keyHandler,
        PojoBytes pojoBytes,
        H kind
    ) {
        return InternalFactory.create(keyHandler, kind, pojoBytes);
    }

    private MapsMemoizers() {
    }
}
