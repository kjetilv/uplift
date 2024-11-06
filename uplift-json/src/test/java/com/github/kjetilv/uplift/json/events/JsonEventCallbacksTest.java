package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonEventCallbacksTest {

    @Test
    void arr() {
        Callbacks myCallbacks = Events.parse(
            callbacks(),
            //language=json
            """
                {
                  "els": [1, 2, "a", "b"],
                  "foo": ["tip", true, [ 3, 4 ], []],
                  "bar": [{"zit": "quz"}, 4]
                }
                """
        );
        assertThat(((MyCallbacks)myCallbacks).getStuff()).containsExactlyElementsOf(lines("""
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
            """)
        );
    }

    @Test
    void arrSimple() {
        Callbacks myCallbacks = Events.parse(
            callbacks(),
            //language=json
            """
                [1, 2, "a"]
                """
        );
        assertThat(((MyCallbacks)myCallbacks).getStuff()).containsExactlyElementsOf(lines("""
            arrayStarted
              number:1
              number:2
              string:a
            arrayEnded
            """)
        );
    }

    @Test
    void arrSimpleLong() {
        Callbacks myCallbacks = Events.parse(
            callbacks(),
            //language=json
            """
                [1, 2, 3, 4, 5, 6, 7, 8]
                """
        );
        assertThat(((MyCallbacks)myCallbacks).getStuff()).containsExactlyElementsOf(lines("""
            arrayStarted
              number:1
              number:2
              number:3
              number:4
              number:5
              number:6
              number:7
              number:8
            arrayEnded
            """)
        );
    }

    @Test
    void arrSimpleNest() {
        Callbacks myCallbacks = Events.parse(
            callbacks(),
            //language=json
            """
                [1, 2, []]
                """
        );
        assertThat(((MyCallbacks)myCallbacks).getStuff()).containsExactlyElementsOf(lines("""
            arrayStarted
              number:1
              number:2
              arrayStarted
              arrayEnded
            arrayEnded
            """)
        );
    }

    @Test
    void arrSimpleNest2() {
        Callbacks myCallbacks = Events.parse(
            callbacks(),
            //language=json
            """
                [1, 2, [3]]
                """
        );
        assertThat(((MyCallbacks)myCallbacks).getStuff()).containsExactlyElementsOf(lines("""
            arrayStarted
              number:1
              number:2
              arrayStarted
                number:3
              arrayEnded
            arrayEnded
            """)
        );
    }

    @Test
    void arrObj() {
        Callbacks myCallbacks = Events.parse(
            callbacks(),
            //language=json
            """
                [1, {}]
                """
        );
        assertThat(((MyCallbacks)myCallbacks).getStuff()).containsExactlyElementsOf(lines("""
            arrayStarted
              number:1
              objectStarted
              objectEnded
            arrayEnded
            """)
        );
    }

    @Test
    void obj() {
        MyCallbacks myCallbacks = (MyCallbacks) Events.parse(
            callbacks(),
            //language=json
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
        assertThat(myCallbacks.getStuff()).containsExactlyElementsOf(lines("""
            objectStarted
              field:foo string:bar
              field:zot number:5
              field:obj2 objectStarted
                field:oops truth:true
              objectEnded
            objectEnded
            """)
        );
    }

    @Test
    void parse() {
        MyCallbacks myCallbacks = (MyCallbacks) Events.parse(
            callbacks(),
            //language=json
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
        assertThat(myCallbacks.getStuff()).containsExactlyElementsOf(lines(
            """
                objectStarted
                  field:foo string:bar
                  field:zot number:5
                  field:obj2 objectStarted
                    field:oops truth:true
                  objectEnded
                objectEnded
                """)
        );
    }

    @Test
    void parseMap() {
        MyCallbacks callbacks = (MyCallbacks) callbacks();
        Events.parse(
            callbacks,
            //language=json
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

    private static List<String> lines(String text) {
        return Arrays.stream(text.split("\\s+")).toList();
    }

    private static Callbacks callbacks() {
        return new MyCallbacks();
    }
}
