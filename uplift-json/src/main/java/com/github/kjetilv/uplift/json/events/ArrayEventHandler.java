package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.tokens.Token;

import static com.github.kjetilv.uplift.json.tokens.TokenType.*;

final class ArrayEventHandler extends AbstractEventHandler {

    ArrayEventHandler(AbstractEventHandler scope, Callbacks callbacks) {
        super(scope, callbacks);
    }

    @Override
    public EventHandler apply(Token token) {
        return switch (token.type()) {
            case BEGIN_OBJECT -> new ObjectEventHandler(this, objectStarted());
            case BEGIN_ARRAY -> new ArrayEventHandler(this, arrayStarted());
            case STRING -> this.with(string(token));
            case BOOL -> this.with(truth(token));
            case NUMBER -> this.with(number(token));
            case NIL -> this.with(nil());
            case END_ARRAY -> exit(Callbacks::arrayEnded);
            case COMMA -> this;
            case COLON, END_OBJECT -> fail(
                "Malformed array",
                token,
                BEGIN_OBJECT, BEGIN_ARRAY, STRING, BOOL, NUMBER
            );
            case WHITESPACE -> fail("Unexpected token", token);
        };
    }

    @Override
    protected AbstractEventHandler with(Callbacks callbacks) {
        return new ArrayEventHandler(exit(), callbacks);
    }
}
