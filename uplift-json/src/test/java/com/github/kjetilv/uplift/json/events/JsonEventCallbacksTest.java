package com.github.kjetilv.uplift.json.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.kjetilv.uplift.json.Events;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({ "DynamicRegexReplaceableByCompiledPattern" })
public class JsonEventCallbacksTest {

    @Test
    void arr() {
        List<String> tokens = new ArrayList<>();
        MyCallbacks myCallbacks = Events.parse(
            callbacks(tokens),
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
        MyCallbacks myCallbacks = Events.parse(
            callbacks(tokens),
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
        MyCallbacks myCallbacks = Events.parse(
            callbacks(tokens),
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
        List<String> tokens = new ArrayList<>();
        MyCallbacks callbacks = callbacks(tokens);
        Events.parse(
            callbacks,
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
    }

    private static MyCallbacks callbacks(List<String> stuff) {
        return new MyCallbacks(stuff);
    }

    private static final class MyCallbacks implements Events.Callbacks<MyCallbacks> {

        static final AtomicInteger COUNT = new AtomicInteger();

        private final int count;

        private final AtomicBoolean called = new AtomicBoolean();

        private final List<String> stuff;

        private MyCallbacks(List<String> stuff) {
            this.count = COUNT.getAndIncrement();
            this.stuff = stuff;
        }

        @Override
        public MyCallbacks objectStarted() {
            return add("objectStarted");
        }

        @Override
        public MyCallbacks field(String name) {
            return add("field:" + name);
        }

        @Override
        public MyCallbacks objectEnded() {
            return add("objectEnded");
        }

        @Override
        public MyCallbacks arrayStarted() {
            return add("arrayStarted");
        }

        @Override
        public MyCallbacks string(String string) {
            return add("string:" + string);
        }

        @Override
        public MyCallbacks number(Number number) {
            return add("number:" + number);
        }

        @Override
        public MyCallbacks truth(boolean truth) {
            return add("truth:" + truth);
        }

        @Override
        public MyCallbacks nil() {
            return add("nil");
        }

        @Override
        public MyCallbacks arrayEnded() {
            return add("arrayEnded");
        }

        private MyCallbacks add(String event) {
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
