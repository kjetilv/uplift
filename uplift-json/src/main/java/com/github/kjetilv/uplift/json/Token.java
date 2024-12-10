package com.github.kjetilv.uplift.json;

import java.util.Arrays;

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

    record String(char[] chars) implements Token {

        public java.lang.String value() {
            return new java.lang.String(chars);
        }

        @Override
        public TokenType tokenType() {
            return TokenType.STRING;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof String(char[] otherChars) && Arrays.equals(chars, otherChars);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(chars);
        }

        @Override
        public java.lang.String toString() {
            return getClass().getSimpleName() + "[" + new java.lang.String(chars) + "]";
        }
    }

    record Field(char[] chars, int length) implements Token {

        public Field(char[] chars) {
            this(chars, chars.length);
        }

        public java.lang.String value() {
            return new java.lang.String(chars, 0, length);
        }

        @Override
        public TokenType tokenType() {
            return TokenType.STRING;
        }

        private static int mismatch(char[] c1, char[] c2, int length) {
            for (int i = 0; i < length; i++) {
                if (c1[i] != c2[i]) {
                    return length;
                }
            }
            return -1;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Field(char[] otherChars, int otherLength) &&
                   length == otherLength &&
                   mismatch(chars, otherChars, length) < 0;
        }

        @Override
        public int hashCode() {
            int hc = Integer.hashCode(length);
            for (int i = 0; i < length; i++) {
                hc += 31 * chars[i];
            }
            return hc;
        }

        @Override
        public java.lang.String toString() {
            return getClass().getSimpleName() + "[" + new java.lang.String(chars, 0, length) + "]";
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
