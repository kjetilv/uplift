package com.github.kjetilv.uplift.json;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.github.kjetilv.uplift.json.io.JsonWriter;
import com.github.kjetilv.uplift.json.io.Parser;
import com.github.kjetilv.uplift.json.io.Sink;
import com.github.kjetilv.uplift.json.tokens.Scanner;
import com.github.kjetilv.uplift.json.tokens.Token;

final class JsonImpl implements Json {

    @Override
    public Object read(InputStream source) {
        try {
            return new Parser(Scanner.tokens(source).toArray(Token[]::new)).parse();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse: " + source, e);
        }
    }

    @Override
    public Object read(String source) {
        Token[] tokens;
        try {
            tokens = Scanner.tokens(source).toArray(Token[]::new);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to scan " + source.length() + " chars", e);
        }
        try {
            return new Parser(tokens).parse();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse " + source.length() + " chars", e);
        }
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
