package com.github.kjetilv.uplift.json;

import module java.base;
import module uplift.json;

import static java.nio.charset.StandardCharsets.UTF_8;

record JsonImpl(JsonSession jsonSession) implements Json {

    JsonImpl() {
        this(null);
    }

    JsonImpl(JsonSession jsonSession) {
        this.jsonSession = jsonSession == null ? new DefaultJsonSession() : jsonSession;
    }

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
        JsonWrites.write(new StringSink(sb), object);
        return sb.toString();
    }

    @Override
    public byte[] writeBytes(Object object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWrites.write(new StreamSink(baos), object);
        return baos.toByteArray();
    }

    @Override
    public void write(Object object, OutputStream outputStream) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWrites.write(new StreamSink(baos), object);
    }

    private Object process(BytesSource bytesSource) {
        AtomicReference<Object> reference = new AtomicReference<>();
        parse(bytesSource, jsonSession.callbacks(reference::set));
        return reference.get();
    }

    private static final PullParser JSON_PULL_PARSER = new DefaultPullParser();

    private static final TokenResolver ALLOCATOR = new Allocator();

    private static TokenResolver resolve(Callbacks callbacks) {
        return callbacks.tokenResolver().orElse(ALLOCATOR);
    }

    private static class Allocator implements TokenResolver {

        @Override
        public Token.Field get(byte[] bytes, int offset, int length) {
            return new Token.Field(bytes);
        }

        @Override
        public Token.Field get(IntUnaryOperator get, int offset, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[}";
        }
    }
}
