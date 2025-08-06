package com.github.kjetilv.uplift.json.test;

import com.github.kjetilv.uplift.json.BytesSource;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.json.bytes.ByteArrayIntsBytesSource;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonEventCallbacksTest {

    @Test
    void arr() {
        String source =
            //language=json
            """
                {
                  "els": [1, 2, "a", "b"],
                  "foo": ["tip", true, [ 3, 4 ], []],
                  "bar": [{"zit": "quz"}, 4]
                }
                """;

        BytesSource bytesSource = new ByteArrayIntsBytesSource(source.getBytes());
        Callbacks myCallbacks = Json.instance().parse(bytesSource, callbacks());
        assertThat(((MyCallbacks) myCallbacks).getStuff()).containsExactlyElementsOf(lines("""
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
                objectEnded
              number:4
              arrayEnded
            objectEnded
            """)
        );
    }

    @Test
    void obj() {
        String json =
            //language=json
            """
            {
              "foo": "bar",
              "zot": 5,
              "obj2": {
                "oops": true
              }
            }
            """;
        BytesSource bytesSource = new ByteArrayIntsBytesSource(json.getBytes());
        MyCallbacks myCallbacks = (MyCallbacks) Json.instance().parse(bytesSource, callbacks());
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
        String json =
            //language=json
            """
            {
              "foo": "bar",
              "zot": 5,
              "obj2": {
                "oops": true
              }
            }
            """;
        BytesSource bytesSource = new ByteArrayIntsBytesSource(json.getBytes());
        MyCallbacks myCallbacks = (MyCallbacks) Json.instance().parse(bytesSource, callbacks());
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
        String json =
            //language=json
            """
            {
              "foo": "bar",
              "zot": 5,
              "obj2": {
                "oops": true
              }
            }
            """;
        BytesSource bytesSource = new ByteArrayIntsBytesSource(json.getBytes());
        Json.instance().parse(bytesSource, callbacks);
    }

    private static final Pattern WS = Pattern.compile("\\s+");

    private static List<String> lines(String text) {
        return Arrays.stream(WS.split(text)).toList();
    }

    private static Callbacks callbacks() {
        return new MyCallbacks();
    }

}
