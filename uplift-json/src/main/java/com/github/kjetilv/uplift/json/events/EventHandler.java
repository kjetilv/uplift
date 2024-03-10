package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.tokens.Scanner;
import com.github.kjetilv.uplift.json.tokens.Token;

import java.io.InputStream;
import java.io.Reader;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

@FunctionalInterface
public interface EventHandler {

    static Callbacks parse(Callbacks callbacks, InputStream source) {
        return parse(Scanner.tokens(source), callbacks);
    }

    static Callbacks parse(Callbacks callbacks, Reader source) {
        return parse(Scanner.tokens(source), callbacks);
    }

    static Callbacks parse(Callbacks callbacks, String source) {
        return parse(Scanner.tokens(source), callbacks);
    }

    static Callbacks parse(
        Stream<Token> tokens, Callbacks callbacks
    ) {
        return tokens.reduce(
            init(callbacks),
            EventHandler::process,
            noCombine()
        ).callbacks();
    }

    EventHandler process(Token token);

    default Callbacks callbacks() {
        throw new UnsupportedOperationException(this + ": No callbacks");
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
