package com.github.kjetilv.uplift.json;

public final class ArrayEvents extends Events {

    public ArrayEvents(Events surroundingScope, Handler... handlers) {
        super(surroundingScope, handlers);
        emit(Handler::arrayStarted);
    }

    @Override
    public Events process(Token token) {
        return switch (token.type()) {
            case END_ARRAY -> emit(Handler::arrayEnded).surroundingScope();
            case COMMA -> this;
            default -> flatValue(token);
        };
    }
}
