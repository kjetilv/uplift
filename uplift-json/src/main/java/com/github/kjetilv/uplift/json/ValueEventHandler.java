package com.github.kjetilv.uplift.json;

import static com.github.kjetilv.uplift.json.TokenType.BEGIN_ARRAY;
import static com.github.kjetilv.uplift.json.TokenType.BEGIN_OBJECT;
import static com.github.kjetilv.uplift.json.TokenType.BOOL;
import static com.github.kjetilv.uplift.json.TokenType.NIL;
import static com.github.kjetilv.uplift.json.TokenType.NUMBER;
import static com.github.kjetilv.uplift.json.TokenType.STRING;

final class ValueEventHandler extends AbstractEventHandler {

    ValueEventHandler(Callbacks callbacks) {
        this(null, callbacks);
    }

    ValueEventHandler(AbstractEventHandler scope, Callbacks callbacks) {
        super(scope, callbacks);
    }

    @Override
    protected AbstractEventHandler with(Callbacks callbacks) {
        return new ValueEventHandler(exit(), callbacks);
    }

    @Override
    public EventHandler process(Token token) {
        return switch (token.type()) {
            case BEGIN_OBJECT -> new ObjectEventHandler(exit(), objectStarted());
            case BEGIN_ARRAY -> new ArrayEventHandler(exit(), arrayStarted());
            case STRING -> with(string(token)).exit();
            case BOOL -> with(truth(token)).exit();
            case NUMBER -> with(number(token)).exit();
            case NIL -> this.with(nil()).exit();
            case COMMA, COLON, END_OBJECT, END_ARRAY -> fail(
                "Invalid value",
                token,
                BEGIN_OBJECT, BEGIN_ARRAY, STRING, BOOL, NUMBER, NIL
            );
        };
    }
}
