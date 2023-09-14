package com.github.kjetilv.uplift.json;

import java.util.function.Function;

@FunctionalInterface
public interface EventHandler extends Function<Token, EventHandler> {

    static EventHandler create(Callbacks... callbacks) {
        return new ValueEventHandler(callbacks);
    }

    interface Callbacks {

        void objectStarted();

        void field(String name);

        void objectEnded();

        void arrayStarted();

        void arrayEnded();

        void truth(boolean truth);

        void number(Number number);

        void nil();

        void string(String string);
    }

    @Override
    default EventHandler apply(Token token) {
        return process(token);
    }

    EventHandler process(Token token);
}
