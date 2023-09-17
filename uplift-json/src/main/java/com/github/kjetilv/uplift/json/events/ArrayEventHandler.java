package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.tokens.Token;

import static com.github.kjetilv.uplift.json.tokens.TokenType.BEGIN_ARRAY;
import static com.github.kjetilv.uplift.json.tokens.TokenType.BEGIN_OBJECT;
import static com.github.kjetilv.uplift.json.tokens.TokenType.BOOL;
import static com.github.kjetilv.uplift.json.tokens.TokenType.NUMBER;
import static com.github.kjetilv.uplift.json.tokens.TokenType.STRING;

final class ArrayEventHandler extends AbstractEventHandler {

    ArrayEventHandler(AbstractEventHandler scope, Events.Callbacks callbacks) {
        super(scope, callbacks);
    }

    @Override
    protected AbstractEventHandler with(Events.Callbacks callbacks) {
        return new ArrayEventHandler(exit(), callbacks);
    }

    @Override
    public EventHandler process(Token token) {
        return switch (token.type()) {
            case BEGIN_OBJECT -> new ObjectEventHandler(this, objectStarted());
            case BEGIN_ARRAY -> new ArrayEventHandler(this, arrayStarted());
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
}
