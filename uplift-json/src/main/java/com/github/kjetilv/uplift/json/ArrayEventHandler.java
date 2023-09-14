package com.github.kjetilv.uplift.json;

final class ArrayEventHandler extends AbstractEventHandler {

    ArrayEventHandler(EventHandler scope, Callbacks... callbacks) {
        super(scope, callbacks);
        startArray();
    }

    @Override
    protected AbstractEventHandler withCallbacks(Callbacks... callbacks) {
        return new ArrayEventHandler(scope(), callbacks);
    }

    @Override
    public EventHandler process(Token token) {
        return switch (token.type()) {
            case END_ARRAY -> endArray();
            case COMMA -> this;
            default -> flatValue(token);
        };
    }
}
