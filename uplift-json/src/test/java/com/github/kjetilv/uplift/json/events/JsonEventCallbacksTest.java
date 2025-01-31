package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Json;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JsonEventCallbacksTest {

    @Test
    void arr() {
        MyCallbacks myCallbacks = parse(
            //language=json
            """
                {
                  "els": [1, 2, "a", "b"],
                  "foo": ["tip", true, [ 3, 4 ], []],
                  "bar": [{"zit": "quz", "1": "2"}, 4]
                }
                """);
        assertThat(myCallbacks.getStuff()).containsExactlyElementsOf(lines("""
            objectStarted
              field:els arrayStarted
                number:1
                number:2
                str:a
                str:b
              arrayEnded
              field:foo arrayStarted
                str:tip
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
                  field:zit str:quz
                  field:1 str:2
                objectEnded
              number:4
              arrayEnded
            objectEnded
            """)
        );
    }

    @Test
    void arrSimple() {
        MyCallbacks myCallbacks = parse(
            //language=json
            """
                [1, 2, "a"]
                """);
        assertThat(myCallbacks.getStuff()).containsExactlyElementsOf(lines("""
            arrayStarted
              number:1
              number:2
              str:a
            arrayEnded
            """)
        );
    }

    @Test
    void arrSimpleLong() {
        MyCallbacks myCallbacks = parse(
            //language=json
            """
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
        MyCallbacks myCallbacks = parse(
            //language=json
            """
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
        MyCallbacks myCallbacks = parse(
            //language=json
            """
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
        MyCallbacks myCallbacks = parse(
            //language=json
            """
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

    @Test
    void objCommaFail() {
        failSetOn(
            """
                { "foo": "bar", }
                """, "Invalid token `}`"
        );
    }

    @Test
    void objCommaFail2() {
        failOn("""
            { , "foo": "bar" }
            """);
    }

    @Test
    void objCommaFail3() {
        failOn("""
            { , }
            """);
    }

    @Test
    void arrCommaFail() {
        failOn("""
            [ 2, ]
            """);
    }

    @Test
    void arrCommaFail3() {
        failOn("""
            [ , ]
            """);
    }

    @Test
    void arrCommaFail2() {
        failOn("""
            [ , 2 ]
            """);
    }

    @Test
    void objFail() {
        failOn("""
            {
            ,"foo": "bar"
            }
            """);
    }

    @Test
    void objFail2() {
        failOn("""
            {
            5: "bar"
            }
            """);
    }

    @Test
    void objFail3() {
        assertThatThrownBy(() ->
            assertThat(parse("""
                {
                "foo": "bar", "zot"
                }
                """)).isNull()
        ).satisfies(e ->
            assertThat(e.toString()).contains("Failed to set `zot`"));
    }

    @Test
    void objFail4() {
        failSetOn(
            """
                {
                "foo": "bar", "zot":
                }
                """,
            "Failed to set `zot`"
        );
    }

    @Test
    void objFail5() {
        failSetOn(
            """
                {
                "foo": "bar", "zot": ,
                }
                """,
            "Failed to set `zot`"
        );
    }

    @Test
    void obj() {
        MyCallbacks myCallbacks = parse(
            //language=json
            """
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
              field:foo str:bar
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
        MyCallbacks myCallbacks = parse(
            //language=json
            """
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
                  field:foo str:bar
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
        Json.INSTANCE.parse(
            //language=json
            """
                {
                  "foo": "bar",
                  "zot": 5,
                  "obj2": {
                    "oops": true
                  }
                }
                """, callbacks
        );
    }

    private static void failOn(String source) {
        assertThatThrownBy(() ->
            assertThat(parse(source)).isNull()
        ).describedAs("Should not parse %s", source)
            .satisfies(e ->
                assertThat(e.toString()).contains("ParseException"));
    }

    private static void failSetOn(String source, String msg) {
        assertThatThrownBy(() ->
            assertThat(parse(source)).isNull()
        ).describedAs("Should not parse %s", source)
            .satisfies(e ->
                assertThat(e.toString()).contains(msg));
    }

    private static MyCallbacks parse(String source) {
        return (MyCallbacks) Json.INSTANCE.parse(source, callbacks());
    }

    private static List<String> lines(String text) {
        return Arrays.stream(text.split("\\s+"))
            .toList();
    }

    private static Callbacks callbacks() {
        return new MyCallbacks();
    }
}
