package com.github.kjetilv.uplift.json;

import static com.github.kjetilv.uplift.json.TokenType.COLON;
import static com.github.kjetilv.uplift.json.TokenType.COMMA;
import static com.github.kjetilv.uplift.json.TokenType.END_OBJECT;
import static com.github.kjetilv.uplift.json.TokenType.STRING;

public final class ObjectEvents extends Events {

    public ObjectEvents(Events surroundingScope, Handler... handlers) {
        super(surroundingScope, handlers);
        emit(Handler::objectStarted);
    }

    @Override
    public Events process(Token token) {
        return switch (token.type()) {
            case END_OBJECT -> emit(Handler::objectEnded).surroundingScope();
            case COMMA -> this;
            case STRING -> {
                emit(handler -> string(token));
                yield new Skip(surroundingScope(), COLON, newValue(), handlers());
            }
            default -> fail(token, END_OBJECT, COMMA, STRING);
        };
    }
}
