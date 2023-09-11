package com.github.kjetilv.uplift.json;

import static com.github.kjetilv.uplift.json.TokenType.COLON;
import static com.github.kjetilv.uplift.json.TokenType.COMMA;
import static com.github.kjetilv.uplift.json.TokenType.END_OBJECT;
import static com.github.kjetilv.uplift.json.TokenType.STRING;

public class ObjectEvents extends Events {

    public ObjectEvents(Events surroundingScope, Handler... handlers) {
        super(surroundingScope, handlers);
        emit(Handler::objectStarted);
    }

    @Override
    public Events process(Token token) {
        if (token.is(END_OBJECT)) {
            return emit(Handler::objectEnded).surroundingScope().surroundingScope();
        }
        if (token.is(COMMA)) {
            return this;
        }
        if (token.is(STRING)) {
            String fieldName = token.literalString();
            try {
                return expectThen(COLON, newValue());
            } finally {
                emit(handler -> handler.field(fieldName));
            }
        }
        return fail(token, END_OBJECT, COMMA, STRING);
    }

    private ValueEvents newValue() {
        return new ValueEvents(this, handlers());
    }

    private Skip expectThen(TokenType skip, Events next) {
        return new Skip(
            surroundingScope(),
            skip,
            next,
            handlers()
        );
    }
}
