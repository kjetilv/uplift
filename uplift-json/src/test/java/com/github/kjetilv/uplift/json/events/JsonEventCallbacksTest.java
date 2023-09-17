package com.github.kjetilv.uplift.json.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.github.kjetilv.uplift.json.tokens.Scanner;
import com.github.kjetilv.uplift.json.tokens.Token;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({ "DynamicRegexReplaceableByCompiledPattern" })
public class JsonEventCallbacksTest {

    @Test
    void arr() {
        List<String> tokens = new ArrayList<>();
        MyCallbacks myCallbacks = (MyCallbacks) Events.parse(
            handler(tokens),
            """
            {
              "els": [1, 2, "a", "b"],
              "foo": ["tip", true, [ 3, 4 ], []],
              "bar": [{"zit": "quz"}, 4]
            }
            """
        );
        assertThat(myCallbacks.stuff).containsExactlyElementsOf(Arrays.stream(
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
        MyCallbacks myCallbacks = (MyCallbacks) Events.parse(
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
        assertThat(myCallbacks.stuff).containsExactlyElementsOf(Arrays.stream(
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
        MyCallbacks myCallbacks = (MyCallbacks) Events.parse(
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
        assertThat(myCallbacks.stuff).containsExactlyElementsOf(Arrays.stream(
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
        Events.Callbacks callback = new Events.Callbacks() {

        };
        List<String> tokens = new ArrayList<>();
        EventHandler reduce = tokenStream.reduce(
            Events.create(handler(tokens)),
            EventHandler::process,
            (events, events2) -> {
                throw new IllegalStateException(events + "/" + events2);
            }
        );
    }

    private static Events.Callbacks handler(List<String> stuff) {
        return new MyCallbacks(stuff);
    }

    private static final class MyCallbacks implements Events.Callbacks {

        static final AtomicInteger COUNT = new AtomicInteger();

        private final int count;

        private final AtomicBoolean called = new AtomicBoolean();

        private final List<String> stuff;

        private MyCallbacks(List<String> stuff) {
            this.count = COUNT.getAndIncrement();
            this.stuff = stuff;
        }

        @Override
        public Events.Callbacks objectStarted() {
            return add("objectStarted");
        }

        @Override
        public Events.Callbacks field(String name) {
            return add("field:" + name);
        }

        @Override
        public Events.Callbacks objectEnded() {
            return add("objectEnded");
        }

        @Override
        public Events.Callbacks arrayStarted() {
            return add("arrayStarted");
        }

        @Override
        public Events.Callbacks string(String string) {
            return add("string:" + string);
        }

        @Override
        public Events.Callbacks number(Number number) {
            return add("number:" + number);
        }

        @Override
        public Events.Callbacks truth(boolean truth) {
            return add("truth:" + truth);
        }

        @Override
        public Events.Callbacks nil() {
            return add("nil");
        }

        @Override
        public Events.Callbacks arrayEnded() {
            return add("arrayEnded");
        }

        private Events.Callbacks add(String event) {
            if (called.compareAndSet(false, true)) {
                ArrayList<String> moreStuff = new ArrayList<>(stuff);
                moreStuff.add(event);
                return new MyCallbacks(moreStuff);
            }
            throw new IllegalStateException("Called again #" + count + "/" + stuff + ": " + event);
        }

        @Override
        public String toString() {
            return stuff.toString();
        }
    }
}
