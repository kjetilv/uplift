package com.github.kjetilv.uplift.json;

import module java.base;

import static java.nio.charset.StandardCharsets.UTF_8;

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

    Token.Field get(IntUnaryOperator get, int offset, int length);

    TokenResolver DEFAULT = new DefaultTokenResolver();
}
