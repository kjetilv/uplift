package com.github.kjetilv.uplift.json.events;

import java.util.function.Supplier;

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
            case STRING -> skip(
                token,
                () ->
                    new ValueEventHandler<>(this, field(token))
            );
            default -> fail("Malformed object level", token, END_OBJECT, COMMA, STRING);
        };
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    private EventHandler<C> skip(Token token, Supplier<EventHandler<C>> next) {
        return skipToken ->
            switch (skipToken.type()) {
                case COLON -> next.get();
                default -> fail("Expected colon to follow field `" + token.literalString() + "`", skipToken, COLON);
            };
    }
}
