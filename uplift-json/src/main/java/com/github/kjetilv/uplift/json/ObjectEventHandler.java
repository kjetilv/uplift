package com.github.kjetilv.uplift.json;

import static com.github.kjetilv.uplift.json.TokenType.COLON;
import static com.github.kjetilv.uplift.json.TokenType.COMMA;
import static com.github.kjetilv.uplift.json.TokenType.END_OBJECT;
import static com.github.kjetilv.uplift.json.TokenType.STRING;

final class ObjectEventHandler extends EventHandler {

    ObjectEventHandler(EventHandler scope, Callbacks... callbacks) {
        super(scope, callbacks);
        emit(Callbacks::objectStarted);
    }

    @Override
    public EventHandler process(Token token) {
        return switch (token.type()) {
            case END_OBJECT -> emit(Callbacks::objectEnded).scope();
            case COMMA -> this;
            case STRING -> {
                emit(handler -> string(token));
                ValueEventHandler next = new ValueEventHandler(this, callbacks());
                yield eventHandler(shouldSkip ->
                    shouldSkip.is(COLON) ? next : fail(shouldSkip, COLON));
            }
            default -> fail(token, END_OBJECT, COMMA, STRING);
        };
    }
}
