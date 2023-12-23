package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Events;
import com.github.kjetilv.uplift.json.tokens.Scanner;
import com.github.kjetilv.uplift.json.tokens.Token;

import java.io.InputStream;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

@FunctionalInterface
public interface EventHandler<C extends Events.Callbacks<C>> {

    static <C extends Events.Callbacks<C>> C parse(C callbacks, InputStream source) {
        return parse(Scanner.tokens(source), callbacks);
    }

    static <C extends Events.Callbacks<C>> C parse(C callbacks, String source) {
        return parse(Scanner.tokens(source), callbacks);
    }

    EventHandler<C> process(Token token);

    default C callbacks() {
        throw new UnsupportedOperationException(this + ": No callbacks");
    }

    private static <C extends Events.Callbacks<C>> C parse(
        Stream<Token> tokens, C callbacks
    ) {
        return tokens.reduce(
            init(callbacks),
            EventHandler::process,
            noCombine()
        ).callbacks();
    }

    private static <C extends Events.Callbacks<C>> EventHandler<C> init(C callbacks) {
        return new ValueEventHandler<>(callbacks);
    }

    private static <T> BinaryOperator<T> noCombine() {
        return (t1, t2) -> {
            throw new IllegalStateException(t1 + " / " + t2 + " do not combine");
        };
    }
}
