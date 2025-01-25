package com.github.kjetilv.uplift.json;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.uplift.json.bytes.ByteArrayBytesSource;
import com.github.kjetilv.uplift.json.bytes.BytesSourceTokens;
import com.github.kjetilv.uplift.json.bytes.InputStreamBytesSource;
import com.github.kjetilv.uplift.json.bytes.LineSegmentBytesSource;
import com.github.kjetilv.uplift.json.events.ValueCallbacks;
import com.github.kjetilv.uplift.json.io.JsonWriter;
import com.github.kjetilv.uplift.json.io.StreamSink;
import com.github.kjetilv.uplift.json.io.StringSink;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

final class JsonImpl implements Json {

    @Override
    public Object read(InputStream inputStream) {
        return process(new InputStreamBytesSource(inputStream));
    }

    @Override
    public Callbacks parse(String source, Callbacks callbacks) {
        return parse(new ByteArrayBytesSource(source.getBytes(UTF_8)), callbacks);
    }

    @Override
    public Callbacks parseMulti(String source, Callbacks callbacks) {
        return parseMulti(new ByteArrayBytesSource(source.getBytes(UTF_8)), callbacks);
    }

    @Override
    public Callbacks parse(InputStream source, Callbacks callbacks) {
        return parse(new InputStreamBytesSource(source), callbacks);
    }

    @Override
    public Callbacks parseMulti(InputStream source, Callbacks callbacks) {
        return parseMulti(new InputStreamBytesSource(source), callbacks);
    }

    @Override
    public Callbacks parse(LineSegment lineSegment, Callbacks callbacks) {
        BytesSource bytesSource = new LineSegmentBytesSource(lineSegment);
        return parse(bytesSource, callbacks);
    }

    @Override
    public Callbacks parseMulti(LineSegment lineSegment, Callbacks callbacks) {
        BytesSource bytesSource = new LineSegmentBytesSource(lineSegment);
        return parseMulti(bytesSource, callbacks);
    }

    @Override
    public Callbacks parse(BytesSource bytesSource, Callbacks callbacks) {
        TokenResolver tokenResolver = resolve(callbacks);
        Tokens tokens = new BytesSourceTokens(bytesSource, tokenResolver);
        JsonPullParser parser = new JsonPullParser(tokens);
        return parser.pull(callbacks);
    }

    @Override
    public Callbacks parseMulti(BytesSource bytesSource, Callbacks callbacks) {
        TokenResolver tokenResolver = resolve(callbacks);
        Tokens tokens = new BytesSourceTokens(bytesSource, tokenResolver);
        JsonPullParser parser = new JsonPullParser(tokens);
        Callbacks walker = callbacks;
        while (true) {
            walker = parser.pull(walker);
            if (parser.done()) {
                return walker;
            }
            walker = walker.line();
        }
    }

    @Override
    public Object read(byte[] bytes) {
        return process(new ByteArrayBytesSource(bytes));
    }

    @Override
    public Object read(char[] bytes) {
        return read(new String(bytes));
    }

    @Override
    public Object read(String string) {
        return process(new ByteArrayBytesSource(string.getBytes(UTF_8)));
    }

    @Override
    public String write(Object object) {
        StringBuilder sb = new StringBuilder();
        JsonWriter.write(new StringSink(sb), object);
        return sb.toString();
    }

    @Override
    public byte[] writeBytes(Object object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter.write(new StreamSink(baos), object);
        return baos.toByteArray();
    }

    @Override
    public void write(Object object, OutputStream outputStream) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter.write(new StreamSink(baos), object);
    }

    private static final TokenResolver ALLOCATOR = new Allocator();

    private static TokenResolver resolve(Callbacks callbacks) {
        return Optional.ofNullable(callbacks.tokenResolver()).orElse(ALLOCATOR);
    }

    private static Object process(BytesSource bytesSource) {
        AtomicReference<Object> reference = new AtomicReference<>();
        INSTANCE.parse(bytesSource, new ValueCallbacks(reference::set));
        return reference.get();
    }

    private static class Allocator implements TokenResolver {

        @Override
        public Token.Field get(LineSegment lineSegment) {
            return new Token.Field(lineSegment);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[}";
        }
    }
}
