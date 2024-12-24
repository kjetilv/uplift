package com.github.kjetilv.uplift.json;

import com.github.kjetilv.uplift.json.bytes.BytesSourceTokens;
import com.github.kjetilv.uplift.json.bytes.CharSequenceBytesSource;
import com.github.kjetilv.uplift.json.bytes.InputStreamBytesSource;
import com.github.kjetilv.uplift.json.bytes.ReaderBytesSource;
import com.github.kjetilv.uplift.json.events.ValueCallbacks;
import com.github.kjetilv.uplift.json.io.JsonWriter;
import com.github.kjetilv.uplift.json.io.StreamSink;
import com.github.kjetilv.uplift.json.io.StringSink;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

final class JsonImpl implements Json {

    @Override
    public Object read(InputStream inputStream) {
        return process(new InputStreamBytesSource(inputStream));
    }

    @Override
    public Callbacks parse(String source, Callbacks callbacks) {
        return parse(new CharSequenceBytesSource(source), callbacks);
    }

    @Override
    public Callbacks parse(InputStream source, Callbacks callbacks) {
        return parse(new InputStreamBytesSource(source), callbacks);
    }

    @Override
    public Callbacks parse(Reader source, Callbacks callbacks) {
        return parse(new ReaderBytesSource(source), callbacks);
    }

    @Override
    public Callbacks parse(BytesSource bytesSource, Callbacks callbacks) {
        TokenResolver tokenResolver =
            Optional.ofNullable(callbacks.tokenResolver()).orElse(ALLOCATOR);
        Tokens tokens = new BytesSourceTokens(bytesSource, tokenResolver);
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
        return process(new CharSequenceBytesSource(string));
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

    public static final TokenResolver ALLOCATOR = new Allocator();

    private static Object process(BytesSource bytesSource) {
        AtomicReference<Object> reference = new AtomicReference<>();
        INSTANCE.parse(bytesSource, new ValueCallbacks(reference::set));
        return reference.get();
    }

    private static class Allocator implements TokenResolver {

        @Override
        public Token.Field get(byte[] chars, int offset, int length) {
            return new Token.Field(chars, offset, length);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[}";
        }
    }
}
