package com.github.kjetilv.uplift.json;

import static com.github.kjetilv.uplift.json.TokenType.COMMA;
import static com.github.kjetilv.uplift.json.TokenType.END_ARRAY;

public class ArrayEvents extends Events {

    public ArrayEvents(Events surroundingScope, Handler... handlers) {
        super(surroundingScope, handlers);
        emit(Handler::arrayStarted);
    }

    @Override
    public Events process(Token token) {
        if (token.is(END_ARRAY)) {
            return emit(Handler::arrayEnded).surroundingScope();        }
        if (token.is(COMMA)) {
            return this;
        }
        return flatValue(token);
    }
}
