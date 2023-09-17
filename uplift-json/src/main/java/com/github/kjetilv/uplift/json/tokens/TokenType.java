package com.github.kjetilv.uplift.json.tokens;

public enum TokenType {

    BEGIN_OBJECT,

    END_OBJECT,

    BEGIN_ARRAY,

    END_ARRAY,

    STRING(true),

    BOOL(true),

    NUMBER(true),

    COMMA,

    COLON,

    NIL;

    private final boolean printable;

    TokenType() {
        this(false);
    }

    TokenType(boolean printable) {
        this.printable = printable;
    }

    boolean printable() {
        return printable;
    }
}
