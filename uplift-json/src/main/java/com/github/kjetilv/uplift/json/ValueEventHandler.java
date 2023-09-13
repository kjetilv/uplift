package com.github.kjetilv.uplift.json;

final class ValueEventHandler extends EventHandler {

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
