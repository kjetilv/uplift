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
    protected AbstractEventHandler withCallbacks(Callbacks... callbacks) {
        return new ObjectEventHandler(scope(), callbacks);
    }

    @Override
    public EventHandler process(Token token) {
        return switch (token.type()) {
            case END_OBJECT -> endObject();
            case COMMA -> this;
            case STRING -> {
                field(token);
                yield skipped -> skipped.is(COLON)
                    ? value()
                    : fail(skipped, COLON);
            }
            default -> fail(token, END_OBJECT, COMMA, STRING);
        };
    }
}
