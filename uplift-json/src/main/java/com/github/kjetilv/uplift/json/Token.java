package com.github.kjetilv.uplift.json;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public record Token(
    TokenType type,
    String lexeme,
    Object literal,
    int line,
    int column
) {

    public Token {
        requireNonNull(type, "type");
    }

    String literalString() {
        return literal.toString();
    }

    boolean literalTruth() {
        if (type == TokenType.BOOL) {
            return Objects.equals(lexeme, Scanner.CANONICAL_TRUE);
        }
        throw new IllegalStateException(this + ": Not boolean");
    }

    Number literalNumber() {
        if (type == TokenType.NUMBER) {
            return (Number) literal;
        }
        throw new IllegalStateException(this + ": Not numeric");
    }

    boolean is(TokenType type) {
        return this.type == type;
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
