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
    public static void write(Object object, Sink sink) {
        switch (object) {
            case null -> writeNull(sink);
            case String value -> writeString(value, sink);
            case BigDecimal bigDec -> writeDecimal(bigDec, sink);
            case BigInteger bigInt -> writeInteger(bigInt, sink);
            case Boolean bool -> sink.accept(bool);
            case Number number -> sink.accept(number);
            case Optional<?> optional -> optional.ifPresentOrElse(
                value -> write(value, sink),
                () -> writeNull(sink)
            );
            case URI uri -> writeString(uri.toASCIIString(), sink);
            case URL url -> writeString(url.toExternalForm(), sink);
            case Map<?, ?> map -> writeObject(map, sink);
            case List<?> list -> writeList(list, sink);
            case Set<?> set -> writeSet(set, sink);
            case Collection<?> collection -> writeCollection(collection, sink);
            case Iterable<?> iterable -> writeIterable(iterable, sink);
            case Stream<?> stream -> writeList(stream.toList(), sink);
            default -> writeString(object.toString(), sink);
        }
    }

    private JsonWriter() {
    }

    private static final Pattern QUOTE = Pattern.compile("\"");

    private static void writeString(String value, Sink sink) {
        sink.accept("\"")
            .accept(value.indexOf('"') >= 0
                ? QUOTE.matcher(value).replaceAll("\\\\\"")
                : value)
            .accept("\"");
    }

    private static void writeNull(Sink sink) {
        sink.accept(Canonical.NULL);
    }

    private static void writeDecimal(BigDecimal dec, Sink sink) {
        sink.accept(dec.toPlainString());
    }

    private static void writeInteger(BigInteger inte, Sink sink) {
        sink.accept(inte.toString(10));
    }

    private static void writeObject(Map<?, ?> map, Sink sink) {
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
            write(entry.getValue(), sink);
        }
        sink.accept("}");
    }

    private static void writeIterable(Iterable<?> iterable, Sink sink) {
        Iterator<?> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            writeNonEmptyArray(sink, iterator);
        } else {
            sink.accept("[]");
        }
    }

    private static void writeList(List<?> list, Sink sink) {
        Iterator<?> iterator = list.iterator();
        if (iterator.hasNext()) {
            writeNonEmptyArray(sink, iterator);
        } else {
            sink.accept("[]");
        }
    }

    private static void writeSet(Set<?> list, Sink sink) {
        Iterator<?> iterator = list.iterator();
        if (iterator.hasNext()) {
            writeNonEmptyArray(sink, iterator);
        } else {
            sink.accept("[]");
        }
    }

    private static void writeCollection(Collection<?> collection, Sink sink) {
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
            write(iterator.next(), sink);
        }
        sink.accept("]");
    }
}
