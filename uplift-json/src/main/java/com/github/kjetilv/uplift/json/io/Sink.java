package com.github.kjetilv.uplift.json.io;

import module java.base;
import com.github.kjetilv.uplift.util.RuntimeCloseable;

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
        return "\"" + JsonStrings.escape(value) + "\"";
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

    /// Writes a number. This is the only place numbers are formatted:
    ///
    /// * Integral boxed types go to [#accept(long)], which sinks may write without allocating
    /// * Doubles and floats go to [#accept(double)] and [#accept(float)], which reject NaN and infinity
    /// * Other numbers, such as [BigDecimal] and [BigInteger], are written by their `toString`. For
    ///   [BigDecimal], this uses exponent notation for large and small exponents, so the output stays
    ///   proportional to the number of digits. [BigDecimal#toPlainString()] would write `1e1000000` as a
    ///   million characters.
    ///
    /// @param number Number
    /// @throws IllegalArgumentException If the number is NaN or infinite
    default void accept(Number number) {
        switch (number) {
            case Long l -> accept(l.longValue());
            case Integer i -> accept(i.longValue());
            case Short s -> accept(s.longValue());
            case Byte b -> accept(b.longValue());
            case Double d -> accept(d.doubleValue());
            case Float f -> accept(f.floatValue());
            default -> accept(number.toString());
        }
    }

    /// @param value Value, written in decimal
    default void accept(long value) {
        accept(Long.toString(value));
    }

    /// @param value Value, written as [Double#toString(double)] does
    /// @throws IllegalArgumentException If the value is NaN or infinite, which JSON cannot represent
    default void accept(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Not a JSON number: " + value);
        }
        accept(Double.toString(value));
    }

    /// @param value Value, written as [Float#toString(float)] does
    /// @throws IllegalArgumentException If the value is NaN or infinite, which JSON cannot represent
    default void accept(float value) {
        if (!Float.isFinite(value)) {
            throw new IllegalArgumentException("Not a JSON number: " + value);
        }
        accept(Float.toString(value));
    }

    /// @param c A character
    default void accept(char c) {
        accept(JsonStrings.string(c));
    }

    default Mark mark() {
        var previous = length();
        return () ->
            previous != length();
    }

    /// Writes the string as JSON string content, escaped, without quotes.
    ///
    /// @param string String
    default void acceptEscaped(String string) {
        accept(JsonStrings.escape(string));
    }

    /// Writes the string as a JSON string, escaped and quoted.
    ///
    /// @param string String
    default void acceptQuoted(String string) {
        accept(quoted(string));
    }

    /// Writes the field name of an object member: escaped and quoted, followed by a colon.
    ///
    /// @param field Field name
    default void acceptField(String field) {
        accept("\"" + JsonStrings.escape(field) + "\":");
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
