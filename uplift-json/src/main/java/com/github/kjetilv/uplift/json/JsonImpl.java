package com.github.kjetilv.uplift.json;

import com.github.kjetilv.uplift.json.events.JsonPullParser;
import com.github.kjetilv.uplift.json.events.ValueCallbacks;
import com.github.kjetilv.uplift.json.io.JsonWriter;
import com.github.kjetilv.uplift.json.io.StreamSink;
import com.github.kjetilv.uplift.json.io.StringSink;
import com.github.kjetilv.uplift.json.tokens.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicReference;

final class JsonImpl implements Json {

    @Override
    public Object read(InputStream inputStream) {
        return process(new InputStreamSource(inputStream));
    }

    @Override
    public Callbacks parse(String source, Callbacks callbacks) {
        return parse(new CharSequenceSource(source), callbacks);
    }

    @Override
    public Callbacks parse(InputStream source, Callbacks callbacks) {
        return parse(new InputStreamSource(source), callbacks);
    }

    @Override
    public Callbacks parse(Reader source, Callbacks callbacks) {
        return parse(new CharsSource(source), callbacks);
    }

    @Override
    public Callbacks parse(Source source, Callbacks callbacks) {
        Tokens tokens = new Tokens(source);
        JsonPullParser parser = new JsonPullParser(tokens);
        if (callbacks.multi()) {
            Callbacks walker = callbacks;
            while (true) {
                walker = parser.pull(walker);
                if (parser.done()) {
                    return walker;
                }
                walker = walker.line();
            }
        }
        return parser.pull(callbacks);
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
        INSTANCE.parse(source, new ValueCallbacks(reference::set));
        return reference.get();
    }

}
