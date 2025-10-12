package com.github.kjetilv.uplift.json.samplegen;

import com.github.kjetilv.uplift.json.BytesSource;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.json.bytes.ByteArrayIntsBytesSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class JsonEventCallbacksTest {

    @Test
    void arr() {
        var source =
            //language=json
            """
                {
                  "els": [1, 2, "a", "b"],
                  "foo": ["tip", true, [ 3, 4 ], []],
                  "bar": [{"zit": "quz"}, 4]
                }
                """;

        BytesSource bytesSource = new ByteArrayIntsBytesSource(source.getBytes());
        var myCallbacks = Json.instance().parse(bytesSource, callbacks());
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
        var json =
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
        var myCallbacks = (MyCallbacks) Json.instance().parse(bytesSource, callbacks());
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
        var json =
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
        var myCallbacks = (MyCallbacks) Json.instance().parse(bytesSource, callbacks());
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
        var callbacks = (MyCallbacks) callbacks();
        var json =
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
