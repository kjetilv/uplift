package com.github.kjetilv.uplift.json;

import java.io.InputStream;
import java.util.function.Function;
import java.util.stream.Stream;

@FunctionalInterface
public interface EventHandler extends Function<Token, EventHandler> {

    static EventHandler parse(Callbacks handler, InputStream source) {
        return reduce(handler, Scanner.tokens(source));
    }

    static EventHandler parse(Callbacks handler, String source) {
        return reduce(handler, Scanner.tokens(source));
    }

    static EventHandler create(Callbacks... callbacks) {
        return new ValueEventHandler(callbacks);
    }

    interface Callbacks {

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

    @Override
    default EventHandler apply(Token token) {
        return process(token);
    }

    EventHandler process(Token token);

    private static EventHandler reduce(Callbacks handler, Stream<Token> tokens) {
        return tokens
            .reduce(
                create(handler),
                EventHandler::process,
                (events, events2) -> {
                    throw new IllegalStateException(events + "/" + events2);
                }
            );
    }
}
