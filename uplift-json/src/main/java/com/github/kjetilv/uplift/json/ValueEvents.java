package com.github.kjetilv.uplift.json;

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
}
