package com.github.kjetilv.uplift.json.io;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class JsonWriter {

    @SuppressWarnings("ChainOfInstanceofChecks")
    public static void write(Sink sink, Object object) {
        switch (object) {
            case null -> writeNull(sink);
            case String s -> writeString(sink, s);
            case BigDecimal d -> writeDecimal(sink, d);
            case BigInteger i -> writeInteger(sink, i);
            case Boolean b -> writeBool(sink, b);
            case Number n -> sink.accept(n);
            case Optional<?> o -> o.ifPresentOrElse(
                value -> write(sink, value),
                () -> writeNull(sink)
            );
            case URI uri -> writeString(sink, uri.toASCIIString());
            case URL url -> writeString(sink, url.toExternalForm());
            case Map<?, ?> map -> writeObject(sink, map);
            case List<?> list -> writeList(sink, list);
            case Set<?> set -> writeSet(sink, set);
            case Collection<?> coll -> writeCollection(sink, coll);
            case Iterable<?> it -> writeIterable(sink, it);
            case Stream<?> str -> writeList(sink, str.toList());
            default -> writeString(sink, object.toString());
        }
    }

    private JsonWriter() {
    }

    private static final Pattern QUOTE = Pattern.compile("\"");

    private static void writeString(Sink sink, String value) {
        sink.accept("\"")
            .accept(value.indexOf('"') >= 0
                ? QUOTE.matcher(value).replaceAll("\\\\\"")
                : value)
            .accept("\"");
    }

    private static void writeNull(Sink sink) {
        sink.accept(Canonical.NULL);
    }

    private static void writeBool(Sink sink, Boolean bool) {
        sink.accept(bool
            ? Canonical.TRUE
            : Canonical.FALSE);
    }

    private static void writeDecimal(Sink sink, BigDecimal dec) {
        sink.accept(dec.toPlainString());
    }

    private static void writeInteger(Sink sink, BigInteger inte) {
        sink.accept(inte.toString(10));
    }

    private static void writeObject(Sink sink, Map<?, ?> map) {
        if (map.isEmpty()) {
            sink.accept("{}");
            return;
        }
        sink.accept("{");
        Sink.Mark mark = sink.mark();
        for (Map.Entry<?, ?> value : map.entrySet()) {
            if (mark.moved()) {
                sink.accept(",");
            }
            Map.Entry<?, ?> entry = value;
            sink.accept("\"").accept(entry.getKey()).accept("\":");
            write(sink, entry.getValue());
        }
        sink.accept("}");
    }

    private static void writeIterable(Sink sink, Iterable<?> iterable) {
        Iterator<?> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            writeNonEmptyArray(sink, iterator);
        } else {
            sink.accept("[]");
        }
    }

    private static void writeList(Sink sink, List<?> list) {
        Iterator<?> iterator = list.iterator();
        if (iterator.hasNext()) {
            writeNonEmptyArray(sink, iterator);
        } else {
            sink.accept("[]");
        }
    }

    private static void writeSet(Sink sink, Set<?> list) {
        Iterator<?> iterator = list.iterator();
        if (iterator.hasNext()) {
            writeNonEmptyArray(sink, iterator);
        } else {
            sink.accept("[]");
        }
    }

    private static void writeCollection(Sink sink, Collection<?> collection) {
        Iterator<?> iterator = collection.iterator();
        if (iterator.hasNext()) {
            writeNonEmptyArray(sink, iterator);
        } else {
            sink.accept("[]");
        }
    }

    private static void writeNonEmptyArray(Sink sink, Iterator<?> iterator) {
        sink.accept("[");
        Sink.Mark mark = sink.mark();
        while (iterator.hasNext()) {
            if (mark.moved()) {
                sink.accept(",");
            }
            write(sink, iterator.next());
        }
        sink.accept("]");
    }
}
