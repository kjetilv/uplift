package com.github.kjetilv.uplift.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

final class JsonWriter {

    @SuppressWarnings("ChainOfInstanceofChecks")
    static void write(Object object, Sink sink) {
        if (object == null) {
            writeNull(sink);
            return;
        }
        if (object instanceof Optional<?> optional) {
            writeOptional(optional, sink);
            return;
        }
        if (object instanceof Map<?, ?> map) {
            writeObject(map, sink);
            return;
        }
        if (object instanceof Iterable<?> list) {
            writeArray(list, sink);
            return;
        }
        if (object instanceof BigDecimal bigDecimal) {
            writeDecimal(bigDecimal, sink);
            return;
        }
        if (object instanceof String value) {
            writeString(value, sink);
            return;
        }
        if (object instanceof Boolean value) {
            sink.accept(value);
            return;
        }
        if (object instanceof Number value) {
            sink.accept(value);
            return;
        }
        if (object instanceof URI uri) {
            writeString(uri.toASCIIString(), sink);
            return;
        }
        if (object instanceof URL url) {
            writeString(url.toExternalForm(), sink);
            return;
        }
        String str = object.toString();
        if (str.equals(objectToString(object))) {
            throw new IllegalArgumentException("Bad object for JSON: " + str);
        }
        writeString(str, sink);
    }

    private JsonWriter() {
    }

    private static final Pattern QUOTE = Pattern.compile("\"");

    private static void writeString(String value, Sink sink) {
        boolean quoted = value.indexOf('"') >= 0;
        String str = quoted
            ? QUOTE.matcher(value).replaceAll("\\\\\"")
            : value;
        sink.accept("\"").accept(str).accept("\"");
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static void writeOptional(Optional<?> optional, Sink sink) {
        optional.ifPresentOrElse(
            value ->
                write(value, sink),
            () ->
                writeNull(sink)
        );
    }

    private static void writeNull(Sink sink) {
        sink.accept("null");
    }

    private static void writeDecimal(BigDecimal dec, Sink sink) {
        String value = dec.toPlainString();
        sink.accept(value.startsWith("0.") ? value.substring(1) : value);
    }

    private static void writeObject(Map<?, ?> map, Sink sink) {
        if (map.isEmpty()) {
            sink.accept("{}");
            return;
        }
        sink.accept("{ ");
        Sink.Mark mark = sink.mark();
        map.forEach((field, value) -> {
            if (mark.moved()) {
                sink.accept(", ");
            }
            sink.accept("\"").accept(field).accept("\": ");
            write(value, sink);
        });
        sink.accept(" }");
    }

    private static void writeArray(Iterable<?> list, Sink sink) {
        if (list.iterator().hasNext()) {
            sink.accept("[ ");
            Sink.Mark mark = sink.mark();
            list.forEach(value -> {
                if (mark.moved()) {
                    sink.accept(", ");
                }
                write(value, sink);
            });
            sink.accept(" ]");
            return;
        }
        sink.accept("[]");
    }

    private static String objectToString(Object object) {
        return object.getClass().getName() + "@" + Integer.toHexString(object.hashCode());
    }
}
