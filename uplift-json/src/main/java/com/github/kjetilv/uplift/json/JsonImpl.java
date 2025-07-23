package com.github.kjetilv.uplift.json;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.uplift.json.bytes.ByteArrayIntsBytesSource;
import com.github.kjetilv.uplift.json.bytes.BytesSourceTokens;
import com.github.kjetilv.uplift.json.bytes.InputStreamIntsBytesSource;
import com.github.kjetilv.uplift.json.bytes.LineSegmentBytesSource;
import com.github.kjetilv.uplift.json.callbacks.ValueCallbacks;
import com.github.kjetilv.uplift.json.io.JsonWriter;
import com.github.kjetilv.uplift.json.io.StreamSink;
import com.github.kjetilv.uplift.json.io.StringSink;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongToIntFunction;

import static java.nio.charset.StandardCharsets.UTF_8;

final class JsonImpl implements Json {

    @Override
    public Object read(InputStream inputStream) {
        return process(new InputStreamIntsBytesSource(inputStream));
    }

    @Override
    public Callbacks parse(String source, Callbacks callbacks) {
        return parse(new ByteArrayIntsBytesSource(source.getBytes(UTF_8)), callbacks);
    }

    @Override
    public Callbacks parseMulti(String source, Callbacks callbacks) {
        return parseMulti(new ByteArrayIntsBytesSource(source.getBytes(UTF_8)), callbacks);
    }

    @Override
    public Callbacks parse(InputStream source, Callbacks callbacks) {
        return parse(new InputStreamIntsBytesSource(source), callbacks);
    }

    @Override
    public Callbacks parseMulti(InputStream source, Callbacks callbacks) {
        return parseMulti(new InputStreamIntsBytesSource(source), callbacks);
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
        return JSON_PULL_PARSER.pull(tokens, callbacks);
    }

    @Override
    public Callbacks parseMulti(BytesSource bytesSource, Callbacks callbacks) {
        TokenResolver tokenResolver = resolve(callbacks);
        Tokens tokens = new BytesSourceTokens(bytesSource, tokenResolver);
        Callbacks walker = callbacks;
        while (true) {
            walker = JSON_PULL_PARSER.pull(tokens, walker);
            if (tokens.done()) {
                return walker;
            }
            walker = walker.line();
        }
    }

    @Override
    public Object read(byte[] bytes) {
        return process(new ByteArrayIntsBytesSource(bytes));
    }

    @Override
    public Object read(char[] bytes) {
        return read(new String(bytes));
    }

    @Override
    public Object read(String string) {
        return process(new ByteArrayIntsBytesSource(string.getBytes(UTF_8)));
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

    private static final PullParser JSON_PULL_PARSER = new DefaultPullParser();

    private static final TokenResolver ALLOCATOR = new Allocator();

    private static TokenResolver resolve(Callbacks callbacks) {
        return callbacks.tokenResolver().orElse(ALLOCATOR);
    }

    private static Object process(BytesSource bytesSource) {
        AtomicReference<Object> reference = new AtomicReference<>();
        INSTANCE.parse(bytesSource, new ValueCallbacks(reference::set));
        return reference.get();
    }

    private static class Allocator implements TokenResolver {

        @Override
        public Token.Field get(LineSegment segment, long offset, long length) {
            return new Token.Field(segment);
        }

        @Override
        public Token.Field get(LongToIntFunction get, long offset, long length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[}";
        }
    }
}
