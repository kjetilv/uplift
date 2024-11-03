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
import java.util.function.Supplier;
import java.util.stream.Stream;

final class JsonImpl implements Json {

    @Override
    public Object read(InputStream source) {
        Token[] tokens = toArray(
            Tokens.stream(source), () ->
                "Failed to scan " + source
        );
        return parse(
            tokens, () ->
                "Failed to parse " + tokens.length + " tokens"
        );
    }

    @Override
    public Object read(String source) {
        Token[] tokens = toArray(
            Tokens.stream(source), () ->
                "Failed to scan " + source.length() + " chars"
        );
        return parse(
            tokens, () ->
                "Failed to parse " + tokens.length + " tokens/" + source.length() + " chars"
        );
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

    private static Token[] toArray(Stream<Token> stream, Supplier<String> error) {
        try {
            return stream.toArray(Token[]::new);
        } catch (Exception e) {
            throw new IllegalStateException(error.get(), e);
        }
    }

    private static Object parse(Token[] tokens, Supplier<String> error) {
        try {
            return new Parser(tokens).parse();
        } catch (Exception e) {
            throw new IllegalStateException(error.get(), e);
        }
    }
}
