package com.github.kjetilv.uplift.json;

public interface TokenResolver {

    Token.Field get(byte[] chars, int offset, int length);
}
