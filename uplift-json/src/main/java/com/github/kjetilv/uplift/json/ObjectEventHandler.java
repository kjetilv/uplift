package com.github.kjetilv.uplift.json;

import static com.github.kjetilv.uplift.json.TokenType.COLON;
import static com.github.kjetilv.uplift.json.TokenType.COMMA;
import static com.github.kjetilv.uplift.json.TokenType.END_OBJECT;
import static com.github.kjetilv.uplift.json.TokenType.STRING;

public final class ObjectEventHandler extends EventHandler {

    public ObjectEventHandler(EventHandler scope, Handler... handlers) {
        super(scope, handlers);
        emit(Handler::objectStarted);
    }

    @Override
    public EventHandler process(Token token) {
        return switch (token.type()) {
            case END_OBJECT -> emit(Handler::objectEnded).scope();
            case COMMA -> this;
            case STRING -> {
                emit(handler -> string(token));
                yield new Skip(scope(), COLON, newValue(), handlers());
            }
            default -> fail(token, END_OBJECT, COMMA, STRING);
        };
    }
}
