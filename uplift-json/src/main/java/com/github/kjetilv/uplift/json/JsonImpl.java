package com.github.kjetilv.uplift.json;

import com.github.kjetilv.uplift.json.io.JsonWriter;
import com.github.kjetilv.uplift.json.io.Parser;
import com.github.kjetilv.uplift.json.io.StreamSink;
import com.github.kjetilv.uplift.json.io.StringSink;
import com.github.kjetilv.uplift.json.tokens.Scanner;
import com.github.kjetilv.uplift.json.tokens.Token;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

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
}
