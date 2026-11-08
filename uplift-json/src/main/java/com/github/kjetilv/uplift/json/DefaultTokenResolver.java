package com.github.kjetilv.uplift.json;

import module java.base;

/// Creates fields for all names. Recently seen names are reused from a small cache, so that repeated names
/// do not allocate. The cache is shared between threads: its slots hold immutable entries, and a lost update
/// only costs a new allocation.
///
/// Lookup reads the name eight bytes at a time. The first and last eight bytes, and the length, select the
/// slot. They also decide equality for names of up to 16 bytes; longer names compare the bytes between too.
final class DefaultTokenResolver implements TokenResolver {

    private final Entry[] cache = new Entry[CACHE_SIZE];

    @Override
    public Token.Field get(byte[] bytes, int offset, int length) {
        long first;
        long last;
        if (length >= Long.BYTES) {
            first = (long) LONGS.get(bytes, offset);
            last = (long) LONGS.get(bytes, offset + length - Long.BYTES);
        } else {
            first = last = partial(bytes, offset, length);
        }
        var slot = slot(first, last, length);
        var entry = cache[slot];
        if (entry != null && entry.matches(first, last, length, bytes, offset)) {
            return entry.field();
        }
        var copy = Arrays.copyOfRange(bytes, offset, offset + length);
        var field = new Token.Field(copy, length, hash(copy), new String(copy, StandardCharsets.UTF_8));
        if (length <= MAX_CACHED_LENGTH) {
            cache[slot] = new Entry(field, first, last);
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

    /// A cached field, with the words used to find and compare it
    private record Entry(Token.Field field, long first, long last) {

        boolean matches(long first, long last, int length, byte[] bytes, int offset) {
            return field.length() == length &&
                   this.first == first &&
                   this.last == last &&
                   (length <= 2 * Long.BYTES ||
                    Arrays.equals(
                        field.bytes(), Long.BYTES, length - Long.BYTES,
                        bytes, offset + Long.BYTES, offset + length - Long.BYTES
                    ));
        }
    }

    private static final int CACHE_BITS = 12;

    private static final int CACHE_SIZE = 1 << CACHE_BITS;

    private static final int MAX_CACHED_LENGTH = 64;

    private static final VarHandle LONGS = MethodHandles.byteArrayViewVarHandle(
        long[].class,
        ByteOrder.LITTLE_ENDIAN
    );

    /// @return Up to seven bytes, little-endian, zero above
    private static long partial(byte[] bytes, int offset, int length) {
        var word = 0L;
        for (var i = length - 1; i >= 0; i--) {
            word = word << 8 | bytes[offset + i] & 0xFFL;
        }
        return word;
    }

    @SuppressWarnings("NumericCastThatLosesPrecision")
    private static int slot(long first, long last, int length) {
        var mixed = (first * 0x9E3779B97F4A7C15L ^ last * 0xC2B2AE3D27D4EB4FL) + length;
        return (int) (mixed * 0x9E3779B97F4A7C15L >>> Long.SIZE - CACHE_BITS);
    }

    /// @return The same value as [Arrays#hashCode(byte\[\])]
    private static int hash(byte[] bytes) {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[]";
    }
}
