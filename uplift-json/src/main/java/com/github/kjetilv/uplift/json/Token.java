package com.github.kjetilv.uplift.json;

import java.util.Objects;

import static com.github.kjetilv.uplift.json.TokenType.*;
import static java.util.Objects.requireNonNull;

public record Token(
    TokenType type,
    String lexeme,
    Object literal,
    int line,
    int column
) {

    public Token(String lexeme) {
        this(STRING, lexeme, lexeme, 0, 0);
    }

    public Token(TokenType type, String lexeme, Object literal) {
        this(type, lexeme, literal, 0, 0);
    }

    public Token {
        requireNonNull(type, "type");
    }

    public String literalString() {
        return literal == lexeme ? lexeme : String.valueOf(literal);
    }

    public int charAt(int i) {
        return literalString().charAt(i);
    }

    public boolean literalTruth() {
        if (type == TokenType.BOOL) {
            return lexeme.charAt(0) == 't';
        }
        throw new IllegalStateException(this + ": Not boolean");
    }

    public Number literalNumber() {
        if (type == TokenType.NUMBER) {
            return (Number) literal;
        }
        throw new IllegalStateException(this + ": Not numeric");
    }

    public boolean is(TokenType type) {
        return this.type == type;
    }

    public boolean not(TokenType type) {
        return this.type != type;
    }

    private String printableValue() {
        if (type().printable()) {
            int length = lexeme.length();
            String printed = length > 10 ? lexeme.substring(0, 9) + "⋯" + " [" + length + "]" : lexeme;
            return printed + ":";
        }
        return "";
    }

    public static final Token BEGIN_OBJECT_TOKEN = canonicalToken(BEGIN_OBJECT);

    public static final Token COLON_TOKEN = canonicalToken(COLON);

    public static final Token COMMA_TOKEN = canonicalToken(COMMA);

    public static final Token END_OBJECT_TOKEN = canonicalToken(END_OBJECT);

    public static final Token BEGIN_ARRAY_TOKEN = canonicalToken(BEGIN_ARRAY);

    public static final Token END_ARRAY_TOKEN = canonicalToken(END_ARRAY);

    public static final Token CANONICAL_WHITESPACE = canonicalToken(TokenType.WHITESPACE);

    public static final Token FALSE_TOKEN = new Token(BOOL, "false", false);

    public static final Token TRUE_TOKEN = new Token(BOOL, "true", true);

    public static final Token NULL_TOKEN = new Token(NULL, "null", null);

    private static Token canonicalToken(TokenType type) {
        return new Token(type, null, null, -1, -1);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Token token && Objects.equals(lexeme, token.lexeme) && type == token.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, lexeme);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + printableValue() + type + " @ " + line + ":" + column + "]";
    }
}
