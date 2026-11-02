package com.github.kjetilv.uplift.json;

import com.github.kjetilv.uplift.json.io.ReadException;

import module java.base;

/// A valid token in the wrong place.
public final class ParseException extends ReadException {

    private final Token token;

    private final TokenType[] expected;

    public ParseException(Object source, Token token, TokenType... expected) {
        this(source, -1, token, expected);
    }

    /// @param source   Source of the error, for the message
    /// @param position Approximate byte offset in the input, or -1 if not known
    /// @param token    Token found
    /// @param expected Token types expected
    public ParseException(Object source, long position, Token token, TokenType... expected) {
        this(source + ": Invalid token `" + token + "`, expected " + tokens(expected), position, List.of(), token, expected);
    }

    private ParseException(String text, long position, List<String> fieldPath, Token token, TokenType[] expected) {
        super(text, position, fieldPath);
        this.token = token;
        this.expected = expected;
    }

    @Override
    protected ReadException withFieldPath(List<String> fieldPath) {
        return new ParseException(text(), position(), fieldPath, token, expected);
    }

    public Collection<TokenType> getExpected() {
        return Arrays.asList(expected);
    }

    public Token getToken() {
        return token;
    }

    private static String tokens(TokenType... expected) {
        if (expected.length == 1) {
            return expected[0].toString();
        }
        return Arrays.stream(expected)
            .map(Enum::name)
            .collect(Collectors.joining(", "));
    }
}
