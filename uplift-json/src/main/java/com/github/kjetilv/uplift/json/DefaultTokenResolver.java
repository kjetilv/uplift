package com.github.kjetilv.uplift.json;

import module java.base;

final class DefaultTokenResolver implements TokenResolver {

    @Override
    public Token.Field get(byte[] bytes, int offset, int length) {
        return new Token.Field(Arrays.copyOfRange(bytes, offset, offset + length));
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[]";
    }
}
