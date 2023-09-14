package com.github.kjetilv.uplift.json;

final class ArrayEventHandler extends AbstractEventHandler {

    ArrayEventHandler(EventHandler scope, Callbacks... callbacks) {
        super(scope, callbacks);
        emit(Callbacks::arrayStarted);
    }

    @Override
    public EventHandler process(Token token) {
        return switch (token.type()) {
            case END_ARRAY -> emit(Callbacks::arrayEnded).scope();
            case COMMA -> this;
            default -> flatValue(token);
        };
    }
}
