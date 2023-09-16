package com.github.kjetilv.uplift.json;

final class ValueEventHandler extends AbstractEventHandler {

    ValueEventHandler(Callbacks... callbacks) {
        this(null, callbacks);
    }

    ValueEventHandler(AbstractEventHandler scope, Callbacks... callbacks) {
        super(scope, callbacks);
    }

    @Override
    protected AbstractEventHandler with(Callbacks... callbacks) {
        return new ValueEventHandler(scope(), callbacks);
    }

    @Override
    public EventHandler process(Token token) {
        return value(token);
    }
}
