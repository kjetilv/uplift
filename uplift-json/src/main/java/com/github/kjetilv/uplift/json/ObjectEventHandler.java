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
        return new ObjectEventHandler(exit(), callbacks);
    }

    @Override
    public EventHandler process(Token token) {
        return switch (token.type()) {
            case END_OBJECT -> exit(Callbacks::objectEnded);
            case COMMA -> this;
            case STRING -> colonToken ->
                colonToken.is(COLON)
                    ? new ValueEventHandler(this, field(token))
                    : fail(
                        "Expected colon to follow field `" + token.literalString() + "`",
                        colonToken,
                        COLON
                    );
            default -> fail("Malformed object level", token, END_OBJECT, COMMA, STRING);
        };
    }
}
