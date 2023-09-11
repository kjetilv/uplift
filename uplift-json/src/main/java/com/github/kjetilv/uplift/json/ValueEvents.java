package com.github.kjetilv.uplift.json;

public class ValueEvents extends Events {

    public ValueEvents(Handler... handlers) {
        this(null, handlers);
    }

    public ValueEvents(Events surroundingScope, Handler... handlers) {
        super(surroundingScope, handlers);
    }

    @Override
    public Events process(Token token) {
        return value(token);
    }
}
