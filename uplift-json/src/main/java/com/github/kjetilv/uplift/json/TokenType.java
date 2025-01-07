package com.github.kjetilv.uplift.json;

import java.util.Arrays;

public enum TokenType {

    BEGIN_ARRAY(true),
    BEGIN_OBJECT(true),
    BOOL(true),
    COLON,
    COMMA,
    END_ARRAY,
    END_OBJECT,
    NULL(true),
    NUMBER(true),
    STRING(true);

    private final boolean value;

    TokenType() {
        this(false);
    }

    TokenType(boolean value) {
        this.value = value;
    }

    public boolean isValue() {
        return value;
    }

    public static TokenType[] valueTokens() {
        return Arrays.stream(values()).filter(t -> t.value).toArray(TokenType[]::new);
    }
}
