package com.github.kjetilv.uplift.json;

import static java.util.Objects.requireNonNull;

record Token(
    TokenType type,
    String lexeme,
    Object literal,
    int line,
    int column
) {

    Token {
        requireNonNull(type, "type");
    }

    boolean is(TokenType type, TokenType... or) {
        if (this.type == requireNonNull(type, "type")) {
            return true;
        }
        for (TokenType orType: or) {
            if (this.type == orType) {
                return true;
            }
        }
        return false;
    }

    private String printableValue() {
        if (type().printable()) {
            int length = lexeme.length();
            String printed = length > 10 ? lexeme.substring(0, 9) + "⋯" + " [" + length + "]" : lexeme;
            return printed + ":";
        }
        return "";
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + printableValue() + type + " @ " + line + ":" + column + "]";
    }
}
