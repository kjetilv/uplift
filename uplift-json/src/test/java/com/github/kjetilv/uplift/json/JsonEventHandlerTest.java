package com.github.kjetilv.uplift.json;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class JsonEventHandlerTest {

    @Test
    void arr() {
        Stream<Token> tokenStream = Scanner.tokens(
            """
            {
              "els": [1, 2, "a", "b"],
              "foo": ["tip", true, [ 3, 4 ], []]
            }
            """);
        List<String> tokens = new ArrayList<>();
        try {
            tokenStream.reduce(
                (EventHandler) new ValueEventHandler(handler(tokens)),
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
            EventHandler.Handler handler = handler(tokens);
            EventHandler rootEventHandler = new ValueEventHandler(handler);
            tokenStream.reduce(
                rootEventHandler,
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
    void parse() {
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
            EventHandler reduce = tokenStream.reduce(
                new ValueEventHandler(handler(tokens)),
                EventHandler::process,
                (events, events2) -> {
                    throw new IllegalStateException(events + "/" + events2);
                }
            );
            System.out.println(reduce);
        } finally {
            System.out.println(tokens);
        }
    }

    private static EventHandler.Handler handler(List<String> stuff) {
        return new EventHandler.Handler() {

            @Override
            public void objectStarted() {
                stuff.add("objectStarted");
            }

            @Override
            public void objectEnded() {
                stuff.add("objectEnded");
            }

            @Override
            public void arrayStarted() {
                stuff.add("arrayStarted");
            }

            @Override
            public void arrayEnded() {
                stuff.add("arrayEnded");
            }

            @Override
            public void truth(boolean truth) {
                stuff.add("truth:" + truth);
            }

            @Override
            public void number(Number number) {
                stuff.add("number:" + number);
            }

            @Override
            public void nil() {
                stuff.add("nil");
            }

            @Override
            public void string(String string) {
                stuff.add("string:" + string);
            }

            @Override
            public String toString() {
                return stuff.toString();
            }
        };
    }
}
