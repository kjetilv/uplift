package com.github.kjetilv.uplift.json;

import java.util.Objects;

public final class Skip extends Events {

    private final TokenType type;

    private final Events next;

    public Skip(Events surroundingScope, TokenType type, Events next, Handler... handlers) {
        super(surroundingScope, handlers);
        this.type = Objects.requireNonNull(type, "type");
        this.next = Objects.requireNonNull(next, "next");
    }

    @Override
    public Events process(Token token) {
        return token.is(type) ? next : fail(token, type);
    }
}
