package com.github.kjetilv.uplift.json;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class JsonEventsTest {

    @Test
    void arr() {
        Stream<Token> tokenStream = Scanner.tokens(
            """
            {
              "els": [1, 2, "a", "b"],
              "foo": ["tip", true, [ 3, 4 ]]
            }
            """);
        List<String> tokens = new ArrayList<>();
        try {
            Events.Handler handler = handler(tokens);
            Events rootEvents = new ValueEvents(handler);
            Events reduce = tokenStream.reduce(
                rootEvents,
                Function::apply,
                (events, events2) -> {
                    throw new IllegalStateException(events + "/" + events2);
                }
            );
        } finally {
            System.out.println(tokens);
        }
    }

    @Test
    void obj() {
        Stream<Token> tokenStream = Scanner.tokens(
            """
            {
              "foo": "bar",
              "zot": 5,
              "obj2": {
                "oops": true
              }
            }
            """);
        List<String> tokens = new ArrayList<>();
        try {
            Events.Handler handler = handler(tokens);
            Events rootEvents = new ValueEvents(handler);
            Events reduce = tokenStream.reduce(
                rootEvents,
                Function::apply,
                (events, events2) -> {
                    throw new IllegalStateException(events + "/" + events2);
                }
            );
        } finally {
            System.out.println(tokens);
        }
    }

    private static Events.Handler handler(List<String> tokens) {
        Events.Handler handler = new Events.Handler() {

            @Override
            public void objectStarted() {
                tokens.add("objectStarted");
            }

            @Override
            public void objectEnded() {
                tokens.add("objectEnded");
            }

            @Override
            public void arrayStarted() {
                tokens.add("arrayStarted");
            }

            @Override
            public void arrayEnded() {
                tokens.add("arrayEnded");
            }

            @Override
            public void truth(boolean truth) {
                tokens.add("truth:" + truth);
            }

            @Override
            public void number(Number number) {
                tokens.add("number:" + number);
            }

            @Override
            public void nil() {
                tokens.add("nil");
            }

            @Override
            public void string(String string) {
                tokens.add("string:" + string);
            }

            @Override
            public String toString() {
                return tokens.toString();
            }
        };
        return handler;
    }
}
