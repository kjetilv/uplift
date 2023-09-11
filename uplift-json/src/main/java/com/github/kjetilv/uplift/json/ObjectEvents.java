package com.github.kjetilv.uplift.json;

import java.util.Objects;

import static com.github.kjetilv.uplift.json.TokenType.COLON;
import static com.github.kjetilv.uplift.json.TokenType.COMMA;
import static com.github.kjetilv.uplift.json.TokenType.END_OBJECT;
import static com.github.kjetilv.uplift.json.TokenType.STRING;

public class ObjectEvents extends Events {

    public ObjectEvents(Path path, Events surroundingScope, Handler... handlers) {
        super(path, surroundingScope, handlers);
        emit(Handler::objectStarted);
    }

    @Override
    public Events process(Token token) {
        if (token.is(END_OBJECT)) {
            emit(Handler::objectEnded);
            return surroundingScope().pop().surroundingScope();
        }
        if (token.is(COMMA)) {
            return this;
        }
        if (token.is(STRING)) {
            String fieldName = token.literalString();
            try {
                return handler(fieldName, (s1, colonToken) ->
                    colonToken.is(COLON)
                        ? new ValueEvents(path().push(fieldName), this, handlers())
                        : fail(token, COLON));
            } finally {
                emit(handler -> handler.field(fieldName));
            }
        }
        return fail(token, END_OBJECT, COMMA, STRING);
    }

    @Override
    protected Events push(String name) {
        return new ObjectEvents(path().push(Objects.requireNonNull(name, "name")), surroundingScope(), handlers());
    }

    @Override
    protected Events pop() {
        return new ObjectEvents(path().pop(), surroundingScope(), handlers());
    }
}
