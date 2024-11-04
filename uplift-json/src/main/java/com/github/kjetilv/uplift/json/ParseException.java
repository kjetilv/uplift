package com.github.kjetilv.uplift.json;

import com.github.kjetilv.uplift.json.events.EventHandler;
import com.github.kjetilv.uplift.json.tokens.Token;
import com.github.kjetilv.uplift.json.tokens.TokenType;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

public final class ParseException extends RuntimeException {

    private final Token token;

    private final TokenType[] expected;

    public ParseException(EventHandler handler, Token token, TokenType... expected) {
        super(handler + ": Invalid token " + token + ", expected one of " + tokens(expected));
        this.token = token;
        this.expected = expected.clone();
    }

    public Collection<TokenType> getExpected() {
        return EnumSet.copyOf(Arrays.asList(expected));
    }

    public Token getToken() {
        return token;
    }

    private static String tokens(TokenType... expected) {
        return Arrays.stream(expected).map(Enum::name).collect(Collectors.joining(", "));
    }
}
