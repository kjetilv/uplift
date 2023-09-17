package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Events;
import com.github.kjetilv.uplift.json.tokens.Token;

import static com.github.kjetilv.uplift.json.tokens.TokenType.BEGIN_ARRAY;
import static com.github.kjetilv.uplift.json.tokens.TokenType.BEGIN_OBJECT;
import static com.github.kjetilv.uplift.json.tokens.TokenType.BOOL;
import static com.github.kjetilv.uplift.json.tokens.TokenType.NIL;
import static com.github.kjetilv.uplift.json.tokens.TokenType.NUMBER;
import static com.github.kjetilv.uplift.json.tokens.TokenType.STRING;

final class ValueEventHandler<C extends Events.Callbacks<C>> extends AbstractEventHandler<C> {

    ValueEventHandler(C callbacks) {
        this(null, callbacks);
    }

    ValueEventHandler(AbstractEventHandler<C> scope, C callbacks) {
        super(scope, callbacks);
    }

    @Override
    protected AbstractEventHandler<C> with(C callbacks) {
        return new ValueEventHandler<>(exit(), callbacks);
    }

    @Override
    public EventHandler<C> process(Token token) {
        return switch (token.type()) {
            case BEGIN_OBJECT -> new ObjectEventHandler<>(exit(), objectStarted());
            case BEGIN_ARRAY -> new ArrayEventHandler<>(exit(), arrayStarted());
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
