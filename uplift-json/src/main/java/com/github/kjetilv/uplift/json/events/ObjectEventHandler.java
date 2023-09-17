package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Events;
import com.github.kjetilv.uplift.json.tokens.Token;

import static com.github.kjetilv.uplift.json.tokens.TokenType.COLON;
import static com.github.kjetilv.uplift.json.tokens.TokenType.COMMA;
import static com.github.kjetilv.uplift.json.tokens.TokenType.END_OBJECT;
import static com.github.kjetilv.uplift.json.tokens.TokenType.STRING;

final class ObjectEventHandler<C extends Events.Callbacks<C>> extends AbstractEventHandler<C> {

    ObjectEventHandler(AbstractEventHandler<C> scope, C callbacks) {
        super(scope, callbacks);
    }

    @Override
    protected AbstractEventHandler<C> with(C callbacks) {
        return new ObjectEventHandler<>(exit(), callbacks);
    }

    @Override
    public EventHandler<C> process(Token token) {
        return switch (token.type()) {
            case END_OBJECT -> exit(Events.Callbacks::objectEnded);
            case COMMA -> this;
            case STRING -> colonToken ->
                colonToken.is(COLON)
                    ? new ValueEventHandler<C>(this, field(token))
                    : fail(
                        "Expected colon to follow field `" + token.literalString() + "`",
                        colonToken,
                        COLON
                    );
            default -> fail("Malformed object level", token, END_OBJECT, COMMA, STRING);
        };
    }
}
