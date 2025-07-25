package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.edamame.impl.InternalFactory;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.Map;

/**
 * Factory methods for {@link MapsMemoizer}s.
 */
public final class MapsMemoizers {

    /**
     * For simple maps with {@link String string} keys – or any keys that map naturally to
     * strings via regular {@link Object#toString() toString}
     *
     * @param <I> Type of id's
     * @return {@link MapsMemoizer} for String-keyed maps
     */
    public static <I, H extends HashKind<H>> MapsMemoizer<I, String> create(H kind) {
        return create(null, null, kind);
    }

    /**
     * This method affords control over stored keys. Stored maps will be normalized to use {@link K}'s
     * as map keys, on all levels. The {@code keyHandler} argument provides a callback that will
     * produce {@link K} instances from keys in incoming maps.
     * <p>
     * Since {@link MapsMemoizer} accepts {@link Map Map<?, ?>}, this function needs to accept any
     * input, i.e. {@link Object ?}.
     *
     * @param <I>        Id type
     * @param <K>        Key type
     * @param keyHandler Key handler
     * @return Map memoizer
     */
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
