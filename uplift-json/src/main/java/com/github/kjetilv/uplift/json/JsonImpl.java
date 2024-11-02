package com.github.kjetilv.uplift.json;

import com.github.kjetilv.uplift.json.io.JsonWriter;
import com.github.kjetilv.uplift.json.io.Parser;
import com.github.kjetilv.uplift.json.io.StreamSink;
import com.github.kjetilv.uplift.json.io.StringSink;
import com.github.kjetilv.uplift.json.tokens.Token;
import com.github.kjetilv.uplift.json.tokens.Tokens;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

final class JsonImpl implements Json {

    @Override
    public Object read(InputStream source) {
        Token[] tokens;
        try {
            tokens = Tokens.stream(source).toArray(Token[]::new);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to scan " + source, e);
        }
        try {
            return new Parser(tokens).parse();
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to parse " + tokens.length + " tokens", e);
        }
    }

    @Override
    public Object read(String source) {
        Token[] tokens;
        try {
            tokens = Tokens.stream(source).toArray(Token[]::new);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to scan " + source.length() + " chars", e);
        }
        try {
            return new Parser(tokens).parse();
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to parse " + tokens.length + " tokens/" + source.length() + " chars", e);
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
}
