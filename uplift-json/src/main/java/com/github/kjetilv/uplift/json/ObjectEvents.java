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
        return new ValueEvents(path(), this, handlers());
    }

    @Override
    protected Events push(String name) {
        return onPath(path().push(Objects.requireNonNull(name, "name")));
    }

    @Override
    protected Events pop() {
        return onPath(path().pop());
    }

    private ObjectEvents onPath(Path pushed) {
        return new ObjectEvents(pushed, surroundingScope(), handlers());
    }

    private Skip expectThen(TokenType skip, Events next) {
        return new Skip(
            path(),
            surroundingScope(),
            skip,
            next,
            handlers()
        );
    }
}
