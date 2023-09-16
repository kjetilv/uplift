package com.github.kjetilv.uplift.json;

import static com.github.kjetilv.uplift.json.TokenType.COLON;
import static com.github.kjetilv.uplift.json.TokenType.COMMA;
import static com.github.kjetilv.uplift.json.TokenType.END_OBJECT;
import static com.github.kjetilv.uplift.json.TokenType.STRING;

final class ObjectEventHandler extends AbstractEventHandler {

    ObjectEventHandler(AbstractEventHandler scope, Callbacks callbacks) {
        super(scope, callbacks);
    }

    @Override
    protected AbstractEventHandler with(Callbacks callbacks) {
        return new ObjectEventHandler(scope(), callbacks);
    }

    @Override
    public EventHandler process(Token token) {
        return switch (token.type()) {
            case END_OBJECT -> close(Callbacks::objectEnded);
            case COMMA -> this;
            case STRING -> colonToken ->
                colonToken.is(COLON)
                    ? new ValueEventHandler(this, field(token))
                    : fail(colonToken, COLON);
            default -> fail(token, END_OBJECT, COMMA, STRING);
        };
    }
}
