package com.github.kjetilv.uplift.json;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.flopp.kernel.LineSegments;

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
    Token.String,
    Token.True {

    default boolean is(TokenType tokenType) {
        return tokenType() == tokenType;
    }

    TokenType tokenType();

    Token BEGIN_OBJECT = new BeginObject(TokenType.BEGIN_OBJECT);

    Token END_OBJECT = new EndObject(TokenType.END_OBJECT);

    Token BEGIN_ARRAY = new BeginArray(TokenType.BEGIN_ARRAY);

    Token END_ARRAY = new EndArray(TokenType.END_ARRAY);

    Token COLON = new Colon(TokenType.COLON);

    Token COMMA = new Comma(TokenType.COLON);

    Token TRUE = new True(TokenType.BOOL);

    Token FALSE = new False(TokenType.BOOL);

    Token NULL = new Null(TokenType.NULL);

    Token SKIP_FIELD = new SkipField(TokenType.STRING);

    record BeginObject(TokenType tokenType) implements Token {

        @Override
        public java.lang.String toString() {
            return "{";
        }
    }

    record EndObject(TokenType tokenType) implements Token {

        @Override
        public java.lang.String toString() {
            return "}";
        }
    }

    record BeginArray(TokenType tokenType) implements Token {

        @Override
        public java.lang.String toString() {
            return "[";
        }
    }

    record EndArray(TokenType tokenType) implements Token {

        @Override
        public java.lang.String toString() {
            return "]";
        }
    }

    record Colon(TokenType tokenType) implements Token {

        @Override
        public java.lang.String toString() {
            return ":";
        }
    }

    record Comma(TokenType tokenType) implements Token {

        @Override
        public java.lang.String toString() {
            return ",";
        }
    }

    record True(TokenType tokenType) implements Token {

        @Override
        public java.lang.String toString() {
            return "true";
        }
    }

    record False(TokenType tokenType) implements Token {

        @Override
        public java.lang.String toString() {
            return "false";
        }
    }

    record Null(TokenType tokenType) implements Token {

        @Override
        public java.lang.String toString() {
            return "null";
        }
    }

    record String(LineSegment lineSegment) implements Token {

        public java.lang.String value() {
            return lineSegment().asString();
        }

        @Override
        public TokenType tokenType() {
            return TokenType.STRING;
        }

        @Override
        public java.lang.String toString() {
            return getClass().getSimpleName() + "[" + value() + "]";
        }
    }

    record SkipField(TokenType tokenType) implements Token {
    }

    record Field(LineSegment lineSegment, int length, int hash) implements Token {

        public Field(LineSegment lineSegment) {
            this(
                lineSegment,
                Math.toIntExact(lineSegment.endIndex() - lineSegment.startIndex()),
                LineSegments.hashCode(lineSegment)
            );
        }

        public java.lang.String value() {
            return lineSegment.asString();
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
        public java.lang.String toString() {
            return getClass().getSimpleName() + "[" + lineSegment.asString() + "]";
        }

    }

    record Number(java.lang.Number number) implements Token {
        @Override
        public TokenType tokenType() {
            return TokenType.NUMBER;
        }

        @Override
        public java.lang.String toString() {
            return getClass().getSimpleName() + "[" + number + "/" + number.getClass() + "]";
        }

    }
}
