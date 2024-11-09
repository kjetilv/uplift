package com.github.kjetilv.uplift.json.tokens;

public enum TokenType {

    BEGIN_OBJECT(true, false),

    END_OBJECT,

    BEGIN_ARRAY(true, false),

    END_ARRAY,

    STRING(true, true),

    BOOL(true, true),

    NUMBER(true, true),

    COMMA,

    COLON,

    NULL(true, false),

    WHITESPACE;

    private final boolean value;

    private final boolean printable;

    TokenType() {
        this(false, false);
    }

    TokenType(boolean value, boolean printable) {
        this.value = value;
        this.printable = printable;
    }

    public boolean isValue() {
        return value;
    }

    boolean printable() {
        return printable;
    }
}
