package com.github.kjetilv.uplift.json;

import module java.base;

public final class ParseException extends RuntimeException {

    private final Token token;

    private final TokenType[] expected;

    public ParseException(Object source, Token token, TokenType... expected) {
        super(source + ": Invalid token `" + token + "`, expected " + tokens(expected));
        this.token = token;
        this.expected = expected;
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
