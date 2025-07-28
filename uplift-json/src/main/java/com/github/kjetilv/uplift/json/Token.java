package com.github.kjetilv.uplift.json;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.flopp.kernel.LineSegments;

@SuppressWarnings("ALL")
public sealed interface Token permits
    Token.BeginObject,
    Token.BeginArray,
    Token.Colon,
    Token.Comma,
    Token.EndArray,
    Token.EndObject,
    Token.False,
    Token.Field,
    Token.SkipField,
    Token.Null,
    Token.Number,
    Token.Str,
    Token.True {

    default boolean is(TokenType tokenType) {
        return tokenType() == tokenType;
    }

    default boolean isValue() {
        return tokenType().isValue();
    }

    TokenType tokenType();

    Token.BeginObject BEGIN_OBJECT = new BeginObject(TokenType.BEGIN_OBJECT);

    Token.EndObject END_OBJECT = new EndObject(TokenType.END_OBJECT);

    Token.BeginArray BEGIN_ARRAY = new BeginArray(TokenType.BEGIN_ARRAY);

    Token.EndArray END_ARRAY = new EndArray(TokenType.END_ARRAY);

    Token.Colon COLON = new Colon(TokenType.COLON);

    Token.Comma COMMA = new Comma(TokenType.COLON);

    Token.True TRUE = new True(TokenType.BOOL);

    Token.False FALSE = new False(TokenType.BOOL);

    Token.Null NULL = new Null(TokenType.NULL);

    Token.SkipField SKIP_FIELD = new SkipField();

    record BeginObject(TokenType tokenType) implements Token {

        @Override
        public String toString() {
            return "{";
        }
    }

    record EndObject(TokenType tokenType) implements Token {

        @Override
        public String toString() {
            return "}";
        }
    }

    record BeginArray(TokenType tokenType) implements Token {

        @Override
        public String toString() {
            return "[";
        }
    }

    record EndArray(TokenType tokenType) implements Token {

        @Override
        public String toString() {
            return "]";
        }
    }

    record Colon(TokenType tokenType) implements Token {

        @Override
        public String toString() {
            return ":";
        }
    }

    record Comma(TokenType tokenType) implements Token {

        @Override
        public String toString() {
            return ",";
        }
    }

    record True(TokenType tokenType) implements Token {

        @Override
        public String toString() {
            return "true";
        }
    }

    record False(TokenType tokenType) implements Token {

        @Override
        public String toString() {
            return "false";
        }
    }

    record Null(TokenType tokenType) implements Token {

        @Override
        public String toString() {
            return "null";
        }
    }

    record Str(LineSegment lineSegment) implements Token {

        public String value() {
            return lineSegment().asString();
        }

        @Override
        public TokenType tokenType() {
            return TokenType.STRING;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + value() + "]";
        }
    }

    record SkipField() implements Token {

        @Override
        public TokenType tokenType() {
            return null;
        }
    }

    record Field(LineSegment lineSegment, int length, int hash) implements Token {

        public Field(LineSegment lineSegment) {
            this(
                lineSegment,
                Math.toIntExact(lineSegment.endIndex() - lineSegment.startIndex()),
                LineSegments.hashCode(lineSegment)
            );
        }

        public String value() {
            return lineSegment.asString();
        }

        public byte[] bytes() {
            return LineSegments.simpleBytes(lineSegment);
        }

        public boolean differsAt(Field other, int index) {
            return lineSegment.byteAt(index) != other.lineSegment().byteAt(index);
        }

        @Override
        public TokenType tokenType() {
            return TokenType.STRING;
        }

        public int length() {
            return length;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            return o instanceof Field(LineSegment otherSegment, int otherLength, _) &&
                   length == otherLength &&
                   lineSegment.matches(otherSegment);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + lineSegment.asString() + "]";
        }
    }

    record Number(java.lang.Number number) implements Token {
        @Override
        public TokenType tokenType() {
            return TokenType.NUMBER;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + number + "/" + number.getClass() + "]";
        }
    }
}
