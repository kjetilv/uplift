package com.github.kjetilv.uplift.json.events;

import java.io.InputStream;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import com.github.kjetilv.uplift.json.tokens.Scanner;
import com.github.kjetilv.uplift.json.tokens.Token;

public final class Events {

    public static final BinaryOperator<EventHandler> NO_COMBINE = (events, events2) -> {
        throw new IllegalStateException(events + "/" + events2);
    };

    public static Callbacks parse(Callbacks callbacks, InputStream source) {
        AbstractEventHandler reduce = (AbstractEventHandler) Scanner.tokens(source).reduce(
            create(callbacks),
            EventHandler::process,
            NO_COMBINE
        );
        return reduce.getCallbacks();
    }

    public static Callbacks parse(Callbacks callbacks, String source) {
        AbstractEventHandler reduce = (AbstractEventHandler)  Scanner.tokens(source).reduce(
            new ValueEventHandler(callbacks),
            EventHandler::process,
            NO_COMBINE
        );
        return reduce.getCallbacks();
    }

    public static EventHandler create(Callbacks callbacks) {
        return new ValueEventHandler(callbacks);
    }

    public interface Callbacks {

        default Callbacks objectStarted() {
            return this;
        }

        default Callbacks field(String name) {
            return this;
        }

        default Callbacks objectEnded() {
            return this;
        }

        default Callbacks arrayStarted() {
            return this;
        }

        default Callbacks string(String string) {
            return this;
        }

        default Callbacks number(Number number) {
            return this;
        }

        default Callbacks truth(boolean truth) {
            return this;
        }

        default Callbacks nil() {
            return this;
        }

        default Callbacks arrayEnded() {
            return this;
        }
    }

    private Events() {
    }

    private static EventHandler reduce(Callbacks callbacks, Stream<Token> tokens) {
        return tokens
            .reduce(
                new ValueEventHandler(callbacks),
                EventHandler::process,
                NO_COMBINE
            );
    }
}
