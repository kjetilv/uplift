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
    public EventHandler handle(Token token) {
        return switch (token.type()) {
            case BEGIN_OBJECT -> new ObjectEventHandler(exitScope(), objectStarted());
            case BEGIN_ARRAY -> new ArrayEventHandler(exitScope(), arrayStarted());
            case STRING -> with(string(token)).exitScope();
            case BOOL -> with(truth(token)).exitScope();
            case NUMBER -> with(number(token)).exitScope();
            case NULL -> this.with(null_()).exitScope();
            case COMMA, COLON, END_OBJECT, END_ARRAY -> fail(
                token,
                BEGIN_OBJECT, BEGIN_ARRAY, STRING, BOOL, NUMBER, NULL
            );
            case WHITESPACE -> fail(token);
        };
    }

    @Override
    public ValueEventHandler with(Callbacks callbacks) {
        return new ValueEventHandler(exitScope(), callbacks);
    }
}
