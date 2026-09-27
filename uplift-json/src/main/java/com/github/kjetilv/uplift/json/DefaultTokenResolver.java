package com.github.kjetilv.uplift.json;

import module java.base;

final class DefaultTokenResolver implements TokenResolver {

    @Override
    public Token.Field get(byte[] bytes, int offset, int length) {
        return new Token.Field(bytes);
    }

    @Override
    public Token.Field get(IntUnaryOperator get, int offset, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[}";
    }
}
