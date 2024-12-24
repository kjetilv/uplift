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

    record String(byte[] bytes) implements Token {

        public java.lang.String value() {
            return new java.lang.String(bytes);
        }

        @Override
        public TokenType tokenType() {
            return TokenType.STRING;
        }

        @Override
        public java.lang.String toString() {
            return getClass().getSimpleName() + "[" + new java.lang.String(bytes) + "]";
        }
    }

    record Field(byte[] bytes, int offset, int length, int hash) implements Token {

        public Field(byte[] chars, int offset, int length) {
            this(chars, offset, length, hc(chars, offset, length));
        }

        public Field(byte[] chars) {
            this(chars, 0, chars.length, hc(chars, 0, chars.length));
        }

        public java.lang.String value() {
            return new java.lang.String(bytes, offset, length);
        }

        public boolean differsAt(Field other, int index) {
            return bytes[offset + index] != other.bytes[other.offset + index];
        }

        public boolean is(byte[] b, int o, int l) {
            if (this.length != l) {
                return false;
            }
            return Arrays.mismatch(
                this.bytes, this.offset, this.offset + this.length,
                b, o, o + l
            ) < 0;
        }

        public boolean is(byte[] bytes) {
            return Arrays.mismatch(this.bytes, bytes) < 0;
        }

        @Override
        public TokenType tokenType() {
            return TokenType.STRING;
        }

        Field in(byte[] cache, int i) {
            return new Field(cache, i, length, hash);
        }

        private static int hc(byte[] c, int o, int l) {
            int hc = Integer.hashCode(l);
            for (int i = 0; i < l; i++) {
                hc += 31 * c[o + i];
            }
            return hc;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            return o instanceof Field(byte[] otherChars, int otherOffset, int otherLength, _) &&
                   length == otherLength &&
                   Arrays.mismatch(
                       bytes, offset, offset + length,
                       otherChars, otherOffset, otherOffset + otherLength
                   ) < 0;
        }

        @Override
        public int hashCode() {
            return hc(bytes, offset, length);
        }

        @Override
        public java.lang.String toString() {
            return getClass().getSimpleName() + "[" + new java.lang.String(bytes, offset, length) + "]";
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
