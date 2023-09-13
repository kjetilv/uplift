package com.github.kjetilv.uplift.json;

import java.util.Objects;

public final class Skip extends EventHandler {

    private final TokenType type;

    private final EventHandler next;

    public Skip(EventHandler scope, TokenType type, EventHandler next, Handler... handlers) {
        super(scope, handlers);
        this.type = Objects.requireNonNull(type, "type");
        this.next = Objects.requireNonNull(next, "next");
    }

    @Override
    public EventHandler process(Token token) {
        return token.is(type) ? next : fail(token, type);
    }
}
