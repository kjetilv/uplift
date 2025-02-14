package com.github.kjetilv.uplift.json;

public interface TokenDescend {

    Level descend(int c);

    sealed interface Level {}

    record Skip(int skip) implements Level {}

    record Found(Token.Field found) implements Level {}
}
