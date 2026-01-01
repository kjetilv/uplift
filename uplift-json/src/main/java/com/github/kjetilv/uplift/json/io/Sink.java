package com.github.kjetilv.uplift.json.io;

import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;

public sealed interface Sink permits ByteChannelSink, StreamSink, StringSink {

    static Sink stream(OutputStream outputStream) {
        return stream(outputStream, null);
    }

    static Sink stream(OutputStream outputStream, Charset charset) {
        return new StreamSink(outputStream, charset);
    }

    static Sink build(StringBuilder stringBuilder) {
        return new StringSink(stringBuilder);
    }

    static Sink buffer(WritableByteChannel byteChannel) {
        return buffer(byteChannel, null);
    }

    static Sink buffer(WritableByteChannel byteChannel, Charset charset) {
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

    Sink accept(String str);

    Mark mark();

    int length();

    @FunctionalInterface
    interface Mark {

        boolean moved();
    }
}
