package com.github.kjetilv.uplift.json;

public class ValueEventHandler extends EventHandler {

    public ValueEventHandler(Handler... handlers) {
        this(null, handlers);
    }

    public ValueEventHandler(EventHandler surroundingScope, Handler... handlers) {
        super(surroundingScope, handlers);
    }

    @Override
    public EventHandler process(Token token) {
        return value(token);
    }
}
