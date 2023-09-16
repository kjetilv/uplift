package com.github.kjetilv.uplift.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({ "DynamicRegexReplaceableByCompiledPattern" })
public class JsonEventCallbacksTest {

    @Test
    void arr() {
        List<String> tokens = new ArrayList<>();
        AbstractEventHandler myCallbacks = (AbstractEventHandler) EventHandler.parse(
            handler(tokens),
            """
            {
              "els": [1, 2, "a", "b"],
              "foo": ["tip", true, [ 3, 4 ], []],
              "bar": [{"zit": "quz"}, 4]
            }
            """
        );
        assertThat(((MyCallbacks) myCallbacks.getCallbacks()).stuff).containsExactlyElementsOf(Arrays.stream(
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
        AbstractEventHandler myCallbacks = (AbstractEventHandler) EventHandler.parse(
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
        assertThat(((MyCallbacks) myCallbacks.getCallbacks()).stuff).containsExactlyElementsOf(Arrays.stream(
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
        AbstractEventHandler myCallbacks = (AbstractEventHandler) EventHandler.parse(
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
        assertThat(((MyCallbacks) myCallbacks.getCallbacks()).stuff).containsExactlyElementsOf(Arrays.stream(
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
        return new MyCallbacks(stuff);
    }

    private static final class MyCallbacks implements AbstractEventHandler.Callbacks {

        static final AtomicInteger COUNT = new AtomicInteger();

        private final int count;

        private final AtomicBoolean called = new AtomicBoolean();

        private final List<String> stuff;

        private MyCallbacks(List<String> stuff) {
            this.count = COUNT.getAndIncrement();
            this.stuff = stuff;
        }

        @Override
        public EventHandler.Callbacks objectStarted() {
            return add("objectStarted");
        }

        @Override
        public EventHandler.Callbacks field(String name) {
            return add("field:" + name);
        }

        @Override
        public EventHandler.Callbacks objectEnded() {
            return add("objectEnded");
        }

        @Override
        public EventHandler.Callbacks arrayStarted() {
            return add("arrayStarted");
        }

        @Override
        public EventHandler.Callbacks string(String string) {
            return add("string:" + string);
        }

        @Override
        public EventHandler.Callbacks number(Number number) {
            return add("number:" + number);
        }

        @Override
        public EventHandler.Callbacks truth(boolean truth) {
            return add("truth:" + truth);
        }

        @Override
        public EventHandler.Callbacks nil() {
            return add("nil");
        }

        @Override
        public EventHandler.Callbacks arrayEnded() {
            return add("arrayEnded");
        }

        private AbstractEventHandler.Callbacks add(String event) {
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
