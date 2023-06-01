package com.github.kjetilv.uplift.json;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

class JsonImpl implements Json {

    @Override
    public Object read(InputStream source) {
        try {
            return new Parser(Scanner.tokens(source).toList()).parse();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse: " + source, e);
        }
    }

    @Override
    public Object read(String source) {
        List<Token> tokens;
        try {
            tokens = Scanner.tokens(source).toList();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to scan, " + source.length() + " chars", e);
        }
        try {
            return new Parser(tokens).parse();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse, " + source.length() + " chars", e);
        }
    }

    @Override
    public String write(Object object) {
        StringBuilder sb = new StringBuilder();
        JsonWriter.write(object, new StringSink(sb));
        return sb.toString();
    }

    @Override
    public void write(Object object, OutputStream outputStream) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter.write(object, new StreamSink(baos));
    }

    private record StreamSink(ByteArrayOutputStream baos) implements Sink {

        @Override
        public Sink accept(String str) {
            try {
                baos.write(str.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to write " + str, e);
            }
            return this;
        }

        @Override
        public Mark mark() {
            int length = baos.size();
            return () -> baos.size() > length;
        }

        @Override
        public int length() {
            return baos.size();
        }
    }

    private record StringSink(StringBuilder sb) implements Sink {

        @Override
        public Sink accept(String str) {
            sb.append(str);
            return this;
        }

        @Override
        public Mark mark() {
            int length = sb.length();
            return () -> sb.length() > length;
        }

        @Override
        public int length() {
            return sb.length();
        }
    }
}
