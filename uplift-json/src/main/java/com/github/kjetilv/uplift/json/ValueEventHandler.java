package com.github.kjetilv.uplift.json;

final class ValueEventHandler extends AbstractEventHandler {

    ValueEventHandler(Callbacks... callbacks) {
        this(null, callbacks);
    }

    ValueEventHandler(EventHandler scope, Callbacks... callbacks) {
        super(scope, callbacks);
    }

    @Override
    public EventHandler process(Token token) {
        return value(token);
    }
}
