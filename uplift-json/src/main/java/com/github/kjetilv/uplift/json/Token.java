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
    Token.Null,
    Token.Number,
    Token.String,
    Token.True {

    default boolean is(TokenType tokenType) {
        return tokenType() == tokenType;
    }

    TokenType tokenType();

    Token BEGIN_OBJECT = new BeginObject();

    Token END_OBJECT = new EndObject();

    Token BEGIN_ARRAY = new BeginArray();

    Token END_ARRAY = new EndArray();

    Token COLON = new Colon();

    Token COMMA = new Comma();

    Token TRUE = new True();

    Token FALSE = new False();

    Token NULL = new Null();

    record BeginObject() implements Token {
        @Override
        public TokenType tokenType() {
            return TokenType.BEGIN_OBJECT;
        }

        @Override
        public java.lang.String toString() {
            return "{";
        }
    }

    record EndObject() implements Token {
        @Override
        public TokenType tokenType() {
            return TokenType.END_OBJECT;
        }

        @Override
        public java.lang.String toString() {
            return "}";
        }
    }

    record BeginArray() implements Token {
        @Override
        public TokenType tokenType() {
            return TokenType.BEGIN_ARRAY;
        }

        @Override
        public java.lang.String toString() {
            return "[";
        }
    }

    record EndArray() implements Token {
        @Override
        public TokenType tokenType() {
            return TokenType.END_ARRAY;
        }

        @Override
        public java.lang.String toString() {
            return "]";
        }
    }

    record Colon() implements Token {
        @Override
        public TokenType tokenType() {
            return TokenType.COLON;
        }

        @Override
        public java.lang.String toString() {
            return ":";
        }
    }

    record Comma() implements Token {
        @Override
        public TokenType tokenType() {
            return TokenType.COMMA;
        }

        @Override
        public java.lang.String toString() {
            return ",";
        }
    }

    record True() implements Token {
        @Override
        public TokenType tokenType() {
            return TokenType.BOOL;
        }

        @Override
        public java.lang.String toString() {
            return "true";
        }
    }

    record False() implements Token {
        @Override
        public TokenType tokenType() {
            return TokenType.BOOL;
        }

        @Override
        public java.lang.String toString() {
            return "false";
        }
    }

    record Null() implements Token {
        @Override
        public TokenType tokenType() {
            return TokenType.NULL;
        }

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
