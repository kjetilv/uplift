package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.tokens.Token;

import static com.github.kjetilv.uplift.json.tokens.TokenType.*;

public final class ValueEventHandler
    extends AbstractEventHandler {

    public ValueEventHandler(Callbacks callbacks) {
        this(null, callbacks);
    }

    ValueEventHandler(AbstractEventHandler scope, Callbacks callbacks) {
        super(scope, callbacks);
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

    @Override
    protected AbstractEventHandler with(Callbacks callbacks) {
        return new ValueEventHandler(exit(), callbacks);
    }
}
