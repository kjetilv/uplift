package com.github.kjetilv.uplift.json.gen;

import module java.base;
import com.github.kjetilv.uplift.json.*;
import com.github.kjetilv.uplift.json.events.*;

import static java.nio.charset.StandardCharsets.UTF_8;

@SuppressWarnings("unused")
public interface JsonRW<T extends Record> {

    // READERS

    default <K, V> T read(Map<K, V> userMap, Class<T> type) {
        var ref = new AtomicReference<T>();
        read(userMap, ref::set);
        return ref.get();
    }

    default <K, V> void read(Map<K, V> userMap, Consumer<T> set) {
        new FromMap(callbacks().apply(set)).accept(userMap);
    }

    default JsonReader<String, T> stringReader() {
        return new StringJsonReader<>(callbacks());
    }

    default JsonReader<String, T> stringReader(JsonSession jsonSession) {
        return new StringJsonReader<>(callbacks(), jsonSession);
    }

    default JsonReader<ReadableByteChannel, T> channelReader(int length) {
        return channelReader((long) length);
    }

    default JsonReader<ReadableByteChannel, T> channelReader(long length) {
        return new ChannelJsonReader<>(callbacks(), length);
    }

    default JsonReader<ReadableByteChannel, T> channelReader(JsonSession jsonSession, int length) {
        return channelReader(jsonSession, (long) length);
    }

    default JsonReader<ReadableByteChannel, T> channelReader(JsonSession jsonSession, long length) {
        return new ChannelJsonReader<>(callbacks(), jsonSession, length);
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

    default JsonReader<Path, T> fileReader() {
        return new PathReader<>(this::channelReader);
    }

    // WRITERS

    default JsonWriter<String, T, StringBuilder> stringWriter() {
        return new StringJsonWriter<>(objectWriter());
    }

    default JsonWriter<byte[], T, ByteArrayOutputStream> bytesWriter() {
        return new BytesJsonWriter<T, ByteArrayOutputStream>(objectWriter(), ByteArrayOutputStream::new);
    }

    default JsonWriter<byte[], T, OutputStream> bytesWriter(OutputStream outputStream) {
        return new BytesJsonWriter<>(objectWriter(), outputStream);
    }

    default JsonWriter<Void, T, WritableByteChannel> channelWriter() {
        return new ChannelJsonWriter<>(objectWriter(), UTF_8);
    }

    default JsonWriter<Void, T, WritableByteChannel> channelWriter(int bufferSize) {
        return new BufferedChannelJsonWriter<>(objectWriter(), UTF_8, bufferSize);
    }

    default JsonWriter<Void, T, WritableByteChannel> chunkedChannelWriter() {
        return chunkedChannelWriter(null, 0);
    }

    default JsonWriter<Void, T, WritableByteChannel> chunkedChannelWriter(int bufferSize) {
        return chunkedChannelWriter(null, bufferSize);
    }

    default JsonWriter<Void, T, WritableByteChannel> chunkedChannelWriter(Charset charset) {
        return chunkedChannelWriter(charset, 0);
    }

    default JsonWriter<Void, T, WritableByteChannel> chunkedChannelWriter(
        Charset charset,
        int bufferSize
    ) {
        return new ChunkedChannelWriter<>(charset, bufferSize, objectWriter());
    }

    default JsonWriter<Path, T, Path> fileWriter() {
        return fileWriter(null, 0);
    }

    default JsonWriter<Path, T, Path> fileWriter(
        int bufferSize
    ) {
        return fileWriter(null, bufferSize);
    }

    default JsonWriter<Path, T, Path> fileWriter(
        Path path,
        OpenOption... openOptions
    ) {
        return fileWriter(path, 0, openOptions);
    }

    default JsonWriter<Path, T, Path> fileWriter(
        Path path,
        int bufferSize,
        OpenOption... openOptions
    ) {
        return new PathWriter<>(channelWriter(bufferSize), path, openOptions);
    }

    // Implementor responsibilities

    Callbacks callbacks(Consumer<T> onDone);

    Function<Consumer<T>, Callbacks> callbacks();

    ObjectWriter<T> objectWriter();
}
