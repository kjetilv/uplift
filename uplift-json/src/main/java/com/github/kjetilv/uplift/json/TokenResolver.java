package com.github.kjetilv.uplift.json;

import module java.base;

import static java.nio.charset.StandardCharsets.UTF_8;

/// Resolves field names, given as UTF-8 bytes, to [Token.Field] instances. Implementations may return
/// canonical instances, and `null` for names they do not know.
@FunctionalInterface
public interface TokenResolver {

    static TokenResolver orDefault(Callbacks callbacks) {
        return callbacks.tokenResolver().orElse(DEFAULT);
    }

    default Token.Field get(Token.Field token) {
        return get(token.bytes());
    }

    default Token.Field get(String token) {
        return get(token.getBytes(UTF_8));
    }

    default Token.Field get(byte[] bytes) {
        return get(bytes, 0, bytes.length);
    }

    default Token.Field get(byte[] bytes, int offset, int length) {
        return get(i -> bytes[i], offset, length);
    }

    /// @param get Returns the byte at an index, as a signed value in the range `-128..127`, as when
    ///            widening a `byte` to `int`. Do not mask with `0xFF`.
    /// @param offset Index of the first byte
    /// @param length Number of bytes
    /// @return The field, or `null` if the resolver does not know it
    Token.Field get(IntUnaryOperator get, int offset, int length);

    TokenResolver DEFAULT = new DefaultTokenResolver();
}
