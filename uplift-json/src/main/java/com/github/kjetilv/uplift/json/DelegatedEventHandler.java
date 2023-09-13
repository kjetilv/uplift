package com.github.kjetilv.uplift.json;

import java.util.function.Function;

final class DelegatedEventHandler extends EventHandler {

    private final Function<Token, EventHandler> eventHandler;

    DelegatedEventHandler(
        EventHandler scope,
        Function<Token, EventHandler> eventHandler,
        Callbacks... callbacks
    ) {
        super(scope, callbacks);
        this.eventHandler = eventHandler;
    }

    @Override
    public EventHandler process(Token token) {
        return eventHandler.apply(token);
    }
}
