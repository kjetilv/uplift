package com.github.kjetilv.uplift.json;

public final class ArrayEventHandler extends EventHandler {

    public ArrayEventHandler(EventHandler surroundingScope, Handler... handlers) {
        super(surroundingScope, handlers);
        emit(Handler::arrayStarted);
    }

    @Override
    public EventHandler process(Token token) {
        return switch (token.type()) {
            case END_ARRAY -> emit(Handler::arrayEnded).surroundingScope();
            case COMMA -> this;
            default -> flatValue(token);
        };
    }
}
