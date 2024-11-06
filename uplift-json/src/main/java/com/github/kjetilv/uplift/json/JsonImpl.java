package com.github.kjetilv.uplift.json;

import com.github.kjetilv.uplift.json.events.EventHandler;
import com.github.kjetilv.uplift.json.events.ValueEventHandler;
import com.github.kjetilv.uplift.json.io.JsonWriter;
import com.github.kjetilv.uplift.json.io.StreamSink;
import com.github.kjetilv.uplift.json.io.StringSink;
import com.github.kjetilv.uplift.json.tokens.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

final class JsonImpl implements Json {

    @Override
    public Object read(InputStream inputStream) {
        return process(new InputStreamSource(inputStream));
    }

    @Override
    public Object read(String string) {
        return process(new CharSequenceSource(string));
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

    private static Object process(Source source) {
        AtomicReference<Object> reference = new AtomicReference<>();
        EventHandler handler = new ValueEventHandler(new ValueCallbacks(reference::set));
        Tokens tokens = new Tokens(source);
        while (true) {
            Token token = tokens.get();
            if (token == null) {
                return reference.get();
            }
            handler = handler.handle(token);
        }
    }
}
