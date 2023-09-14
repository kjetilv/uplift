package com.github.kjetilv.uplift.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({ "DynamicRegexReplaceableByCompiledPattern" })
public class JsonEventCallbacksTest {

    @Test
    void arr() {
        List<String> tokens = new ArrayList<>();
        EventHandler.parse(
            handler(tokens),
            """
            {
              "els": [1, 2, "a", "b"],
              "foo": ["tip", true, [ 3, 4 ], []],
              "bar": [{"zit": "quz"}, 4]
            }
            """
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
        List<String> tokens = new ArrayList<>();
        EventHandler.parse(
            handler(tokens),
            """
            {
              "foo": "bar",
              "zot": 5,
              "obj2": {
                "oops": true
              }
            }
            """
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
        List<String> tokens = new ArrayList<>();
        EventHandler.parse(
            handler(tokens),
            """
            {
              "foo": "bar",
              "zot": 5,
              "obj2": {
                "oops": true
              }
            }
            """
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
    void parseMap() {
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
        EventHandler.Callbacks callback = new EventHandler.Callbacks() {

        };
        List<String> tokens = new ArrayList<>();
        EventHandler reduce = tokenStream.reduce(
            EventHandler.create(handler(tokens)),
            EventHandler::process,
            (events, events2) -> {
                throw new IllegalStateException(events + "/" + events2);
            }
        );
    }

    private static EventHandler.Callbacks handler(List<String> stuff) {
        return new AbstractEventHandler.Callbacks() {

            @Override
            public EventHandler.Callbacks objectStarted() {
                stuff.add("objectStarted");
                return this;
            }

            @Override
            public EventHandler.Callbacks field(String name) {
                stuff.add("field:" + name);
                return this;
            }

            @Override
            public EventHandler.Callbacks objectEnded() {
                stuff.add("objectEnded");
                return this;
            }

            @Override
            public EventHandler.Callbacks arrayStarted() {
                stuff.add("arrayStarted");
                return this;
            }

            @Override
            public EventHandler.Callbacks string(String string) {
                stuff.add("string:" + string);
                return this;
            }

            @Override
            public EventHandler.Callbacks number(Number number) {
                stuff.add("number:" + number);
                return this;
            }

            @Override
            public EventHandler.Callbacks truth(boolean truth) {
                stuff.add("truth:" + truth);
                return this;
            }

            @Override
            public EventHandler.Callbacks nil() {
                stuff.add("nil");
                return this;
            }

            @Override
            public EventHandler.Callbacks arrayEnded() {
                stuff.add("arrayEnded");
                return this;
            }

            @Override
            public String toString() {
                return stuff.toString();
            }
        };
    }
}
