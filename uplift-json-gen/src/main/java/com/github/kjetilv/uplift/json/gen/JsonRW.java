package com.github.kjetilv.uplift.json.gen;

import module java.base;
import com.github.kjetilv.uplift.json.*;
import com.github.kjetilv.uplift.json.events.*;
import com.github.kjetilv.uplift.json.io.ChunkedTransferByteChannelSink;
import com.github.kjetilv.uplift.json.io.DefaultFieldEvents;

import static java.nio.charset.StandardCharsets.UTF_8;

@SuppressWarnings("unused")
public interface JsonRW<T extends Record> {

    default JsonReader<String, T> stringReader() {
        return new StringJsonReader<>(callbacks());
    }

    default JsonReader<String, T> stringReader(JsonSession jsonSession) {
        return new StringJsonReader<>(callbacks(), jsonSession);
    }

    default JsonWriter<String, T, StringBuilder> stringWriter() {
        return new StringJsonWriter<>(objectWriter());
    }

    default JsonReader<InputStream, T> streamReader() {
        return new InputStreamJsonReader<>(callbacks());
    }

    default JsonReader<InputStream, T> streamReader(JsonSession jsonSession) {
        return new InputStreamJsonReader<>(callbacks(), jsonSession);
    }

    default JsonReader<byte[], T> bytesReader() {
        return new BytesJsonReader<>(callbacks());
    }

    default JsonReader<byte[], T> bytesReader(JsonSession jsonSession) {
        return new BytesJsonReader<>(callbacks(), jsonSession);
    }

    default JsonWriter<byte[], T, ByteArrayOutputStream> bytesWriter() {
        return new BytesJsonWriter<>(objectWriter());
    }

    default JsonReader<ReadableByteChannel, T> channelReader(int length) {
        return new ChannelJsonReader<>(callbacks(), length);
    }

    default JsonReader<ReadableByteChannel, T> channelReader(JsonSession jsonSession, int length) {
        return new ChannelJsonReader<>(callbacks(), jsonSession, length);
    }

    default JsonWriter<Void, T, WritableByteChannel> channelWriter() {
        return new ChannelJsonWriter<T>(objectWriter(), UTF_8);
    }

    default JsonWriter<Void, T, WritableByteChannel> channelWriter(int bufferSize) {
        return new BufferedChannelJsonWriter<>(objectWriter(), UTF_8, bufferSize);
    }

    default JsonWriter<Void, T, WritableByteChannel> chunkedChannelWriter(int bufferSize) {
        return chunkedChannelWriter(null, bufferSize);
    }

    default JsonWriter<Void, T, WritableByteChannel> chunkedChannelWriter(Charset charset, int bufferSize) {
        return (t, out) -> {
            try (var sink = new ChunkedTransferByteChannelSink(out, charset, bufferSize)) {
                var events = new DefaultFieldEvents(null, sink);
                objectWriter().write(t, events);
            }
        };
    }

    default <K, V> T read(Map<K, V> userMap, Class<T> type) {
        var ref = new AtomicReference<T>();
        read(userMap, ref::set);
        return ref.get();
    }

    default <K, V> void read(Map<K, V> userMap, Consumer<T> set) {
        new FromMap(callbacks().apply(set))
            .accept(userMap);
    }

    Callbacks callbacks(Consumer<T> onDone);

    Function<Consumer<T>, Callbacks> callbacks();

    ObjectWriter<T> objectWriter();
}
