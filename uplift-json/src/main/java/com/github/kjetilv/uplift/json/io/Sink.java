package com.github.kjetilv.uplift.json.io;

import com.github.kjetilv.uplift.util.RuntimeCloseable;

import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import static com.github.kjetilv.uplift.json.io.Canonical.FALSE;
import static com.github.kjetilv.uplift.json.io.Canonical.TRUE;

public sealed interface Sink extends RuntimeCloseable
    permits AbstractBufferedSink, AbstractEncodingSink, StringSink {

    Pattern QUOTE = Pattern.compile("\"");

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

    static String quoted(String value) {
        var quoted = value.indexOf('"') >= 0;
        var unquoted = quoted
            ? replaceAll(value)
            : value;
        var formatted = "\"%s\"".formatted(unquoted);
        return formatted;
    }

    static String replaceAll(String value) {
        try {
            return QUOTE.matcher(value).replaceAll("\\\\\"");
        } catch (Exception e) {
            throw new IllegalStateException("Could not replace `" + QUOTE.pattern() + "` with `\\\\`: " + value, e);
        }
    }

    default void accept(Object obj) {
        accept(obj.toString());
    }

    default void accept(boolean bool) {
        accept(bool ? TRUE : FALSE);
    }

    default void accept(Number number) {
        accept(number.toString());
    }

    default Mark mark() {
        var previous = length();
        return () ->
            previous != length();
    }

    default void acceptQuoted(String string) {
        accept(quoted(string));
    }

    @Override
    default void close() {
    }

    void accept(String string);

    long length();

    @FunctionalInterface
    interface Mark {

        boolean moved();
    }
}
