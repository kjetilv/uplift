package com.github.kjetilv.uplift.json;

import static com.github.kjetilv.uplift.json.TokenType.COLON;
import static com.github.kjetilv.uplift.json.TokenType.COMMA;
import static com.github.kjetilv.uplift.json.TokenType.END_OBJECT;
import static com.github.kjetilv.uplift.json.TokenType.STRING;

final class ObjectEventHandler extends AbstractEventHandler {

    ObjectEventHandler(EventHandler scope, Callbacks... callbacks) {
        super(scope, callbacks);
        startObject();
    }

    @Override
    public EventHandler process(Token token) {
        return switch (token.type()) {
            case END_OBJECT -> emit(Callbacks::objectEnded).scope();
            case COMMA -> this;
            case STRING -> {
                field(token);
                yield colonAndValue();
            }
            default -> fail(token, END_OBJECT, COMMA, STRING);
        };
    }

    private EventHandler colonAndValue() {
        return shouldSkip -> shouldSkip.is(COLON)
            ? new ValueEventHandler(this, callbacks())
            : fail(shouldSkip, COLON);
    }
}
