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

        void string(String string);

        void number(Number number);

        void truth(boolean truth);

        void nil();

        void arrayEnded();
    }

    @Override
    default EventHandler apply(Token token) {
        return process(token);
    }

    EventHandler process(Token token);
}
