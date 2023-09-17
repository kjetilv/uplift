package com.github.kjetilv.uplift.json.events;

import java.io.InputStream;
import java.util.function.BinaryOperator;

import com.github.kjetilv.uplift.json.Events;
import com.github.kjetilv.uplift.json.tokens.Scanner;
import com.github.kjetilv.uplift.json.tokens.Token;

@FunctionalInterface
public interface EventHandler<C extends Events.Callbacks<C>> {

    static <C extends Events.Callbacks<C>> C parse(C callbacks, InputStream source) {
        return Scanner.tokens(source).reduce(
            (EventHandler<C>) new ValueEventHandler<>(callbacks),
            EventHandler::process,
            noCombine()
        ).callbacks();
    }

    static <C extends Events.Callbacks<C>> C parse(C callbacks, String source) {
        EventHandler<C> root = new ValueEventHandler<>(callbacks);
        return Scanner.tokens(source)
            .reduce(
                root,
                EventHandler::process,
                noCombine()
            )
            .callbacks();
    }

    EventHandler<C> process(Token token);

    default C callbacks() {
        throw new UnsupportedOperationException(this + ": No callbacks");
    }

    private static <T> BinaryOperator<T> noCombine() {
        return (t1, t2) -> {
            throw new IllegalStateException(t1 + " / " + t2 + " do not combine");
        };
    }
}
