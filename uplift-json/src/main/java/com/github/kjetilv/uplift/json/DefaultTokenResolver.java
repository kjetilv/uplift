package com.github.kjetilv.uplift.json;

import module java.base;

/// Creates fields for all names. Recently seen names are reused from a small cache, so that repeated names
/// do not allocate. The cache is shared between threads: its slots hold immutable fields, and a lost update
/// only costs a new allocation.
final class DefaultTokenResolver implements TokenResolver {

    private final Token.Field[] cache = new Token.Field[CACHE_SIZE];

    @Override
    public Token.Field get(byte[] bytes, int offset, int length) {
        var hash = hash(bytes, offset, length);
        var slot = (hash ^ hash >>> 16) & CACHE_SIZE - 1;
        var cached = cache[slot];
        if (cached != null &&
            cached.hash() == hash &&
            Arrays.equals(cached.bytes(), 0, cached.length(), bytes, offset, offset + length)
        ) {
            return cached;
        }
        var copy = Arrays.copyOfRange(bytes, offset, offset + length);
        var field = new Token.Field(copy, length, hash, new String(copy, StandardCharsets.UTF_8));
        if (length <= MAX_CACHED_LENGTH) {
            cache[slot] = field;
        }
        return field;
    }

    @SuppressWarnings("NumericCastThatLosesPrecision")
    @Override
    public Token.Field get(IntUnaryOperator get, int offset, int length) {
        var bytes = new byte[length];
        for (var i = 0; i < length; i++) {
            bytes[i] = (byte) get.applyAsInt(offset + i);
        }
        return new Token.Field(bytes);
    }

    private static final int CACHE_SIZE = 4096;

    private static final int MAX_CACHED_LENGTH = 64;

    /// @return The same value as [Arrays#hashCode(byte\[\])] for the range
    private static int hash(byte[] bytes, int offset, int length) {
        var hash = 1;
        for (var i = offset; i < offset + length; i++) {
            hash = 31 * hash + bytes[i];
        }
        return hash;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[]";
    }
}
