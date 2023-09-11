package com.github.kjetilv.uplift.json;

import java.util.Objects;

public class ValueEvents extends Events {

    public ValueEvents(Path path, Handler... handlers) {
        this(path, null, handlers);
    }

    public ValueEvents(Path path, Events surroundingScope, Handler... handlers) {
        super(path, surroundingScope, handlers);
    }

    @Override
    public Events process(Token token) {
        return value(token);
    }

    @Override
    protected Events push(String name) {
        Path pushed = path().push(Objects.requireNonNull(name, "name"));
        return new ValueEvents(pushed, surroundingScope(), handlers());
    }

    @Override
    protected Events pop() {
        return new ValueEvents(path().pop(), surroundingScope(), handlers());
    }
}
