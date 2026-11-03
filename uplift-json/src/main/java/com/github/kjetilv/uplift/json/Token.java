package com.github.kjetilv.uplift.json;

import module java.base;

import static java.nio.charset.StandardCharsets.UTF_8;

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

    /// String value, decoded once
    record Str(String value) implements Token {

        public Str(byte[] bytes) {
            this(new String(bytes, UTF_8));
        }

        /// @return UTF-8 bytes of the value, as a new array
        public byte[] bytes() {
            return value.getBytes(UTF_8);
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

    /// Field name, as UTF-8 bytes and as a [String], decoded once
    ///
    /// @param bytes  UTF-8 bytes
    /// @param length Number of bytes
    /// @param hash   [Arrays#hashCode(byte\[\])] of the bytes
    /// @param value  The name
    record Field(byte[] bytes, int length, int hash, String value) implements Token {

        public Field(String string) {
            this(string.getBytes(UTF_8), string);
        }

        public Field(byte[] bytes) {
            this(bytes, new String(bytes, UTF_8));
        }

        private Field(byte[] bytes, String value) {
            this(bytes, bytes.length, Arrays.hashCode(bytes), value);
        }

        public byte[] bytes() {
            return bytes;
        }

        public boolean differsAt(Field other, int index) {
            return bytes[index] != other.bytes()[index];
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
            if (o instanceof Field field) {
                if (field.hash != hash) {
                    return false;
                }
                return length == field.length && Arrays.equals(bytes, field.bytes);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + value() + "]";
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
