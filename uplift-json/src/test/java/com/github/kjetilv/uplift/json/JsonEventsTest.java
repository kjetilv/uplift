package com.github.kjetilv.uplift.json;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class JsonEventsTest {

    @Test
    void arr() {
        Stream<Token> scanner = Scanner.tokens((
            """
            {
              "els": [1, 2]
            }
            """
        ));
        Events.Path path = new LinkedPath();
        List<String> tokens = new ArrayList<>();
        try {
            Events.Handler handler = handler(tokens);
            Events rootEvents = new ValueEvents(path, handler);
            Events reduce = scanner.reduce(
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
        Stream<Token> scanner = Scanner.tokens((
            """
            {
              "foo": "bar",
              "zot": 5,
              "obj2": {
                "oops": true
              }
            }
            """
        ));
        Events.Path path = new LinkedPath();
        List<String> tokens = new ArrayList<>();
        try {
            Events.Handler handler = handler(tokens);
            Events rootEvents = new ValueEvents(path, handler);
            Events reduce = scanner.reduce(
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
            public void field(String name) {
                tokens.add("field:" + name);
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

    private static final class LinkedPath implements Events.Path {

        private final LinkedList<String> path;

        private LinkedPath() {
            this(null);
        }

        private LinkedPath(LinkedList<String> path) {
            this.path = path == null ? new LinkedList<>() : path;
        }

        @Override
        public Events.Path push(String name) {
            Objects.requireNonNull(name, "name");
            LinkedList<String> newPath = new LinkedList<>(path);
            newPath.addLast(name);
            return new LinkedPath(newPath);
        }

        @Override
        public Events.Path pop() {
            LinkedList<String> newPath = new LinkedList<>(path);
            newPath.removeLast();
            return new LinkedPath(newPath);
        }

        @Override
        public String toString() {
            return path.isEmpty()
                ? "[]"
                : "[" + String.join("/", path) + "]";
        }
    }
}
