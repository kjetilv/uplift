package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Events;
import com.github.kjetilv.uplift.json.tokens.Token;

import java.util.function.Supplier;

import static com.github.kjetilv.uplift.json.tokens.TokenType.*;

final class ObjectEventHandler<C extends Events.Callbacks<C>>
    extends AbstractEventHandler<C> {

    ObjectEventHandler(AbstractEventHandler<C> scope, C callbacks) {
        super(scope, callbacks);
    }

    @Override
    public EventHandler<C> process(Token token) {
        return switch (token.type()) {
            case END_OBJECT -> exit(Events.Callbacks::objectEnded);
            case COMMA -> this;
            case STRING -> skip(
                token,
                () ->
                    new ValueEventHandler<>(this, field(token))
            );
            default -> fail("Malformed object level", token, END_OBJECT, COMMA, STRING);
        };
    }

    @Override
    protected AbstractEventHandler<C> with(C callbacks) {
        return new ObjectEventHandler<>(exit(), callbacks);
    }

    private EventHandler<C> skip(Token token, Supplier<EventHandler<C>> next) {
        return skipToken ->
            skipToken.type() == COLON
                ? next.get()
                : fail("Expected " + COLON + " to follow field `" + token.literalString() + "`", skipToken, COLON);
    }
}
