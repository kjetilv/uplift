package com.github.kjetilv.uplift.json;

import module java.base;
import com.github.kjetilv.uplift.json.bytes.*;
import com.github.kjetilv.uplift.json.callbacks.DefaultJsonSession;
import com.github.kjetilv.uplift.json.io.JsonWrites;
import com.github.kjetilv.uplift.json.io.Sink;

import static java.nio.charset.StandardCharsets.UTF_8;

record JsonImpl(JsonSession jsonSession) implements Json {

    JsonImpl() {
        this(null);
    }

    JsonImpl(JsonSession jsonSession) {
        this.jsonSession = jsonSession == null ? new DefaultJsonSession() : jsonSession;
    }

    @Override
    public Object read(MemorySegment memorySegment) {
        return process(new MemorySegmentIntsBytesSource(memorySegment));
    }

    @Override
    public Object read(ByteBuffer inputStream) {
        return process(new ByteBufferIntsBytesSource(inputStream));
    }

    @Override
    public Object read(InputStream inputStream) {
        return process(new InputStreamIntsBytesSource(inputStream));
    }

    @Override
    public Object read(BytesSource bytesSource) {
        return process(bytesSource);
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
    public Callbacks parse(ByteBuffer source, Callbacks callbacks) {
        return parse(new ByteBufferIntsBytesSource(source), callbacks);
    }

    @Override
    public Callbacks parseMulti(ByteBuffer source, Callbacks callbacks) {
        return parseMulti(new ByteBufferIntsBytesSource(source), callbacks);
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
    public Callbacks parse(BytesSource bytesSource, Callbacks callbacks) {
        return JSON_PULL_PARSER.pull(
            new BytesSourceTokens(
                bytesSource,
                TokenResolver.orDefault(callbacks)
            ),
            callbacks
        );
    }

    @Override
    public Callbacks parseMulti(BytesSource bytesSource, Callbacks callbacks) {
        var knownTokens = TokenResolver.orDefault(callbacks);
        Tokens tokens = new BytesSourceTokens(bytesSource, knownTokens);
        var walker = callbacks;
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
        var sb = new StringBuilder();
        JsonWrites.write(Sink.build(sb), object);
        return sb.toString();
    }

    @Override
    public byte[] writeBytes(Object object) {
        try (var outputStream = new ByteArrayOutputStream()) {
            write(object, outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to close", e);
        }
    }

    @Override
    public void write(Object object, OutputStream outputStream) {
        JsonWrites.write(Sink.stream(outputStream), object);
    }

    @Override
    public void write(Object object, WritableByteChannel byteChannel) {
        JsonWrites.write(Sink.buffer(byteChannel), object);
    }

    private Object process(BytesSource bytesSource) {
        var ref = new AtomicReference<>();
        parse(bytesSource, jsonSession.callbacks(ref::set));
        return ref.get();
    }

    private static final PullParser JSON_PULL_PARSER = new DefaultPullParser();
}
