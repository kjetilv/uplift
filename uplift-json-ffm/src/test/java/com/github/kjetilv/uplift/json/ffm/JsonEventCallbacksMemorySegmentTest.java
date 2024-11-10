package com.github.kjetilv.uplift.json.ffm;

import com.github.kjetilv.flopp.kernel.segments.LineSegments;
import com.github.kjetilv.flopp.kernel.util.BytesSupplier;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.json.tokens.IntsSource;
import com.github.kjetilv.uplift.json.Source;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class JsonEventCallbacksMemorySegmentTest {

    @Test
    void arr() {
        String source = """
            {
              "els": [1, 2, "a", "b"],
              "foo": ["tip", true, [ 3, 4 ], []],
              "bar": [{"zit": "quz"}, 4]
            }
            """;

        Callbacks myCallbacks = Json.INSTANCE.parse(
            new IntsSource(new BytesSupplier(LineSegments.of(source).longSupplier())), callbacks()
        );
        Assertions.assertThat(((MyCallbacks) myCallbacks).getStuff()).containsExactlyElementsOf(lines("""
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
    void obj() {
        String json = """
            {
              "foo": "bar",
              "zot": 5,
              "obj2": {
                "oops": true
              }
            }
            """;
        Source source = new IntsSource(new BytesSupplier(LineSegments.of(json).longSupplier()));
        MyCallbacks myCallbacks = (MyCallbacks) Json.INSTANCE.parse(source, callbacks());
        Assertions.assertThat(myCallbacks.getStuff()).containsExactlyElementsOf(lines("""
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
        String json = """
            {
              "foo": "bar",
              "zot": 5,
              "obj2": {
                "oops": true
              }
            }
            """;
        Source source = new IntsSource(new BytesSupplier(LineSegments.of(json).longSupplier()));
        MyCallbacks myCallbacks = (MyCallbacks) Json.INSTANCE.parse(source, callbacks()
        );
        Assertions.assertThat(myCallbacks.getStuff()).containsExactlyElementsOf(lines(
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
        String json = """
            {
              "foo": "bar",
              "zot": 5,
              "obj2": {
                "oops": true
              }
            }
            """;
        Source source = new IntsSource(new BytesSupplier(LineSegments.of(json).longSupplier()));
        Json.INSTANCE.parse(source, callbacks);
    }

    private static final Pattern WS = Pattern.compile("\\s+");

    private static List<String> lines(String text) {
        return Arrays.stream(WS.split(text)).toList();
    }

    private static Callbacks callbacks() {
        return new MyCallbacks();
    }

}
