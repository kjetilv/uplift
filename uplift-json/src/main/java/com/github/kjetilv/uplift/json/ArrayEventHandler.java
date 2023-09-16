package com.github.kjetilv.uplift.json;

import static com.github.kjetilv.uplift.json.TokenType.BEGIN_ARRAY;
import static com.github.kjetilv.uplift.json.TokenType.BEGIN_OBJECT;
import static com.github.kjetilv.uplift.json.TokenType.BOOL;
import static com.github.kjetilv.uplift.json.TokenType.NUMBER;
import static com.github.kjetilv.uplift.json.TokenType.STRING;

final class ArrayEventHandler extends AbstractEventHandler {

    ArrayEventHandler(AbstractEventHandler scope, Callbacks callbacks) {
        super(scope, callbacks);
    }

    @Override
    protected AbstractEventHandler with(Callbacks callbacks) {
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
            case END_ARRAY -> exit(Callbacks::arrayEnded);
            case COMMA -> this;
            case COLON, END_OBJECT -> fail(
                "Malformed array",
                token,
                BEGIN_OBJECT, BEGIN_ARRAY, STRING, BOOL, NUMBER
            );
        };
    }
}
