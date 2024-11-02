package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.tokens.Token;

import java.util.function.Supplier;

import static com.github.kjetilv.uplift.json.tokens.TokenType.*;

final class ObjectEventHandler extends AbstractEventHandler {

    ObjectEventHandler(AbstractEventHandler scope, Callbacks callbacks) {
        super(scope, callbacks);
    }

    @Override
    public EventHandler apply(Token token) {
        return switch (token.type()) {
            case END_OBJECT -> exit(Callbacks::objectEnded);
            case COMMA -> this;
            case STRING -> skip(
                token,
                () ->
                    new ValueEventHandler(this, field(token))
            );
            default -> fail("Malformed object level", token, END_OBJECT, COMMA, STRING);
        };
    }

    @Override
    protected AbstractEventHandler with(Callbacks callbacks) {
        return new ObjectEventHandler(exit(), callbacks);
    }

    private EventHandler skip(Token token, Supplier<EventHandler> next) {
        return skipToken ->
            skipToken.type() == COLON
                ? next.get()
                : fail("Expected " + COLON + " to follow field `" + token.literalString() + "`", skipToken, COLON);
    }
}
