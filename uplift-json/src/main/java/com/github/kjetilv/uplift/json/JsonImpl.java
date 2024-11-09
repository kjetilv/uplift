package com.github.kjetilv.uplift.json;

import com.github.kjetilv.uplift.json.events.JsonPull;
import com.github.kjetilv.uplift.json.io.JsonWriter;
import com.github.kjetilv.uplift.json.io.StreamSink;
import com.github.kjetilv.uplift.json.io.StringSink;
import com.github.kjetilv.uplift.json.tokens.CharSequenceSource;
import com.github.kjetilv.uplift.json.tokens.InputStreamSource;
import com.github.kjetilv.uplift.json.tokens.Source;

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
        JsonPull.parse(source, new ValueCallbacks(reference::set));
        return reference.get();
    }
}
