package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.tokens.*;

import java.io.InputStream;
import java.io.Reader;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

@FunctionalInterface
public interface EventHandler {

    static Callbacks parse(Callbacks callbacks, InputStream source) {
        return callbacks(callbacks, new InputStreamSource(source));
    }

    static Callbacks parse(Callbacks callbacks, Reader source) {
        return callbacks(callbacks, new CharsSource(source));
    }

    static Callbacks parse(Callbacks callbacks, String source) {
        return callbacks(callbacks, new CharSequenceSource(source));
    }

    default Callbacks callbacks() {
        throw new UnsupportedOperationException(this + " has no callbacks");
    }

    EventHandler handle(Token token);

    private static Callbacks callbacks(Callbacks callbacks, Source source1) {
        Tokens tokens = new Tokens(source1);
        EventHandler handler = init(callbacks);
        while (true) {
            Token next = tokens.next();
            if (next == null) {
                return handler.callbacks();
            }
            handler = handler.handle(next);
        }
    }

    private static Callbacks parse(
        Stream<Token> tokens, Callbacks callbacks
    ) {
        return tokens.reduce(
            init(callbacks),
            EventHandler::handle,
            noCombine()
        ).callbacks();
    }

    private static EventHandler init(Callbacks callbacks) {
        return new ValueEventHandler(callbacks);
    }

    private static <T> BinaryOperator<T> noCombine() {
        return (t1, t2) -> {
            throw new IllegalStateException(t1 + " / " + t2 + " do not combine");
        };
    }
}
