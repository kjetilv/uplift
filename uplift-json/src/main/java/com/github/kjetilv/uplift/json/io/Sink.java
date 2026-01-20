package com.github.kjetilv.uplift.json.io;

import com.github.kjetilv.uplift.util.RuntimeCloseable;

import java.io.Closeable;
import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;

public sealed interface Sink extends RuntimeCloseable
    permits BufferedByteChannelSink, ByteChannelSink, ChunkedTransferByteChannelSink, StreamSink, StringSink {

    static Sink stream(OutputStream outputStream) {
        return stream(outputStream, null);
    }

    static Sink stream(OutputStream outputStream, Charset charset) {
        return new StreamSink(outputStream, charset);
    }

    static Sink string(StringBuilder stringBuilder) {
        return new StringSink(stringBuilder);
    }

    static Sink channel(WritableByteChannel byteChannel) {
        return channel(byteChannel, null);
    }

    static Sink channel(WritableByteChannel byteChannel, Charset charset) {
        return new ByteChannelSink(byteChannel, charset);
    }

    default Sink accept(Object obj) {
        return accept(obj.toString());
    }

    default Sink accept(boolean bool) {
        return accept(bool ? Canonical.TRUE : Canonical.FALSE);
    }

    default Sink accept(Number number) {
        return accept(number.toString());
    }

    @Override
    default void close() {
    }

    Sink accept(String str);

    default Mark mark() {
        var length = length();
        return () -> length != length();
    }

    long length();

    @FunctionalInterface
    interface Mark {

        boolean moved();
    }
}
