package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.ParseException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JsonEventCallbacksTest {

    @Test
    void arr() {
        MyCallbacks myCallbacks = parse("""
            {
              "els": [1, 2, "a", "b"],
              "foo": ["tip", true, [ 3, 4 ], []],
              "bar": [{"zit": "quz"}, 4]
            }
            """);
        assertThat(myCallbacks.getStuff()).containsExactlyElementsOf(lines("""
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
        MyCallbacks myCallbacks = parse("""
            [1, 2, "a"]
            """);
        assertThat(myCallbacks.getStuff()).containsExactlyElementsOf(lines("""
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
        MyCallbacks myCallbacks = parse("""
            [1, 2, 3, 4, 5, 6, 7, 8]
            """);
        assertThat(myCallbacks.getStuff()).containsExactlyElementsOf(lines("""
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
        MyCallbacks myCallbacks = parse("""
            [1, 2, []]
            """);
        assertThat(myCallbacks.getStuff()).containsExactlyElementsOf(lines("""
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
        MyCallbacks myCallbacks = parse("""
            [1, 2, [3]]
            """);
        assertThat(myCallbacks.getStuff()).containsExactlyElementsOf(lines("""
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
        MyCallbacks myCallbacks = parse("""
            [1, {}]
            """);
        assertThat(myCallbacks.getStuff()).containsExactlyElementsOf(lines("""
            arrayStarted
              number:1
              objectStarted
              objectEnded
            arrayEnded
            """)
        );
    }

    @Test
    void arrObjFail() {
        failOn("[1, {,}]");
        failOn("[1, ,{}]");
        failOn("[,1, 1]");
        failOn("[1, ,{}],");
    }

    private static void failOn(String source) {
        assertThatThrownBy(() -> parse(source)).isInstanceOf(ParseException.class);
    }

    private static MyCallbacks parse(String source) {
        return (MyCallbacks) Events.parse(
            callbacks(),
            source
        );
    }

    @Test
    void obj() {
        MyCallbacks myCallbacks = parse("""
            {
              "foo": "bar",
              "zot": 5,
              "obj2": {
                "oops": true
              }
            }
            """);
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
        MyCallbacks myCallbacks = parse("""
            {
              "foo": "bar",
              "zot": 5,
              "obj2": {
                "oops": true
              }
            }
            """);
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
        return Arrays.stream(text.split("\\s+"))
            .toList();
    }

    private static Callbacks callbacks() {
        return new MyCallbacks();
    }
}
