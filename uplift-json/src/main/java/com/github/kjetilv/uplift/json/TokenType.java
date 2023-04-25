package com.github.kjetilv.uplift.json;

enum TokenType {

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

    private final boolean print;

    TokenType() {
        this(false);
    }

    TokenType(boolean print) {
        this.print = print;
    }

    boolean print() {
        return print;
    }
}
