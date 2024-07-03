package com.github.kjetilv.uplift.json.ffm;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.flopp.kernel.LineSegments;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.events.EventHandler;
import com.github.kjetilv.uplift.json.events.ValueEventHandler;
import com.github.kjetilv.uplift.json.tokens.Scanner;
import com.github.kjetilv.uplift.json.tokens.Source;
import com.github.kjetilv.uplift.json.tokens.Token;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JsonEventCallbacksMemorySegmentTest {

    public static MemorySegment of(String json) {
        return MemorySegment.ofBuffer(byteBuffer(json.getBytes(StandardCharsets.UTF_8)));
    }

    public static LineSegment ls(String json) {
        return LineSegments.of(json,StandardCharsets.UTF_8);
    }

    public static Stream<Token> tokens(Source source) {
        return StreamSupport.stream(new Scanner(source), false);
    }

    static Callbacks parse(Callbacks callbacks, Stream<Token> tokens) {
        return parse(tokens, callbacks);
    }

    static Callbacks parse(Stream<Token> tokens, Callbacks callbacks) {
        return tokens.reduce(
            new ValueEventHandler(callbacks),
            EventHandler::process,
            (_, _) -> {
                throw new IllegalStateException();
            }
        ).callbacks();
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
        Callbacks myCallbacks = parse(
            callbacks(),
            tokens(new MemorySegmentSource(memorySegment, 0L, memorySegment.byteSize()))
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
        String source = """
            {
              "foo": "bar",
              "zot": 5,
              "obj2": {
                "oops": true
              }
            }
            """;
        MyCallbacks myCallbacks = (MyCallbacks) parse(
            callbacks(),
            tokens(new MemorySegmentSource(of(source), 0L, source.length()))
        );
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
        String source = """
            {
              "foo": "bar",
              "zot": 5,
              "obj2": {
                "oops": true
              }
            }
            """;
        MyCallbacks myCallbacks = (MyCallbacks) parse(
            callbacks(),
            tokens(new MemorySegmentSource(of(source), 0L, source.length()))
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
        String source = """
            {
              "foo": "bar",
              "zot": 5,
              "obj2": {
                "oops": true
              }
            }
            """;
        parse(
            callbacks,
            tokens(new MemorySegmentSource(of(source), 0L, source.length()))
        );
    }

    private static final Pattern WS = Pattern.compile("\\s+");

    private static List<String> lines(String text) {
        return Arrays.stream(WS.split(text)).toList();
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
