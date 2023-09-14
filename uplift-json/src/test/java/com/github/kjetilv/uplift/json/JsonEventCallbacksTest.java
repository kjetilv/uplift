package com.github.kjetilv.uplift.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({ "ResultOfMethodCallIgnored", "DynamicRegexReplaceableByCompiledPattern" })
public class JsonEventCallbacksTest {

    @Test
    void arr() {
        Stream<Token> tokenStream = Scanner.tokens(
            """
            {
              "els": [1, 2, "a", "b"],
              "foo": ["tip", true, [ 3, 4 ], []],
              "bar": [{"zit": "quz"}, 4]
            }
            """);
        List<String> tokens = new ArrayList<>();
        tokenStream.reduce(
            EventHandler.create(handler(tokens)),
            Function::apply,
            (events, events2) -> {
                throw new IllegalStateException(events + "/" + events2);
            }
        );
        assertThat(tokens).containsExactlyElementsOf(Arrays.stream(
            """
            objectStarted
              field:els arrayStarted
                number:1
                number:2
                string:a
                string:b
              arrayEnded
              field:foo arrayStarted
                string:tip
                truth:true
                arrayStarted
                  number:3
                  number:4
                arrayEnded
                arrayStarted
                arrayEnded
              arrayEnded
              field:bar arrayStarted
                objectStarted
                  field:zit string:quz
                objectEnded
              number:4
              arrayEnded
            objectEnded
            """.split("\\s+")).toList()
        );
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
        AbstractEventHandler.Callbacks callbacks = handler(tokens);
        EventHandler rootEventHandler = EventHandler.create(callbacks);
        tokenStream.reduce(
            rootEventHandler,
            Function::apply,
            (events, events2) -> {
                throw new IllegalStateException(events + "/" + events2);
            }
        );
        assertThat(tokens).containsExactlyElementsOf(Arrays.stream(
            """
            objectStarted
              field:foo string:bar
              field:zot number:5
              field:obj2 objectStarted
                field:oops truth:true
              objectEnded
            objectEnded
            """.split("\\s+")).toList()
        );
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
        EventHandler reduce = tokenStream.reduce(
            EventHandler.create(handler(tokens)),
            EventHandler::process,
            (events, events2) -> {
                throw new IllegalStateException(events + "/" + events2);
            }
        );
        System.out.println(reduce);
        assertThat(tokens).containsExactlyElementsOf(Arrays.stream(
            """
            objectStarted
              field:foo string:bar
              field:zot number:5
              field:obj2 objectStarted
                field:oops truth:true
              objectEnded
            objectEnded
            """.split("\\s+")).toList()
        );
    }

    private static AbstractEventHandler.Callbacks handler(List<String> stuff) {
        return new AbstractEventHandler.Callbacks() {

            @Override
            public void objectStarted() {
                stuff.add("objectStarted");
            }

            @Override
            public void field(String name) {
                stuff.add("field:" + name);
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
