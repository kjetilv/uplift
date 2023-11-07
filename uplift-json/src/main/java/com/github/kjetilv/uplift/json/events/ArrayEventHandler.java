package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Events;
import com.github.kjetilv.uplift.json.tokens.Token;

import static com.github.kjetilv.uplift.json.tokens.TokenType.*;

final class ArrayEventHandler<C extends Events.Callbacks<C>>
    extends AbstractEventHandler<C> {

    ArrayEventHandler(AbstractEventHandler<C> scope, C callbacks) {
        super(scope, callbacks);
    }

    @Override
    public EventHandler<C> process(Token token) {
        return switch (token.type()) {
            case BEGIN_OBJECT -> new ObjectEventHandler<>(this, objectStarted());
            case BEGIN_ARRAY -> new ArrayEventHandler<>(this, arrayStarted());
            case STRING -> with(string(token));
            case BOOL -> with(truth(token));
            case NUMBER -> with(number(token));
            case NIL -> with(nil());
            case END_ARRAY -> exit(Events.Callbacks::arrayEnded);
            case COMMA -> this;
            case COLON, END_OBJECT -> fail(
                "Malformed array",
                token,
                BEGIN_OBJECT, BEGIN_ARRAY, STRING, BOOL, NUMBER
            );
        };
    }

    @Override
    protected AbstractEventHandler<C> with(C callbacks) {
        return new ArrayEventHandler<>(exit(), callbacks);
    }
}
