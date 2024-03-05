package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Events;
import org.junit.jupiter.api.Test;

import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonEventCallbacksMemorySegmentTest {

    public static MemorySegment of(String json) {
        return MemorySegment.ofBuffer(byteBuffer(json.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void arr() {
        String source = """
            {
              "els": [1, 2, "a", "b"],
              "foo": ["tip", true, [ 3, 4 ], []],
              "bar": [{"zit": "quz"}, 4]
            }
            """;
        MemorySegment memorySegment = of(source);
        Callbacks myCallbacks = Events.parse(
            callbacks(),

            memorySegment,
            0L,
            memorySegment.byteSize()
        );
        assertThat(((MyCallbacks) myCallbacks).getStuff()).containsExactlyElementsOf(lines("""
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
        String source = """
            {
              "foo": "bar",
              "zot": 5,
              "obj2": {
                "oops": true
              }
            }
            """;
        MyCallbacks myCallbacks = (MyCallbacks) Events.parse(
            callbacks(),
            of(source),
            0L,
            source.length()
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
        String source = """
            {
              "foo": "bar",
              "zot": 5,
              "obj2": {
                "oops": true
              }
            }
            """;
        MyCallbacks myCallbacks = (MyCallbacks) Events.parse(
            callbacks(),
            of(source),
            0L,
            source.length()
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
        String source = """
            {
              "foo": "bar",
              "zot": 5,
              "obj2": {
                "oops": true
              }
            }
            """;
        Events.parse(
            callbacks,
            of(source),
            0L,
            source.length()
        );
    }

    private static List<String> lines(String text) {
        return Arrays.stream(text.split("\\s+"))
            .toList();
    }

    private static Callbacks callbacks() {
        return new MyCallbacks();
    }

    private static ByteBuffer byteBuffer(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.allocateDirect(bytes.length);
        bb.put(bytes);
        bb.flip();
        return bb;
    }

}
