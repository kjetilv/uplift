package com.github.kjetilv.uplift.json.io;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class JsonWriter {

    @SuppressWarnings("ChainOfInstanceofChecks")
    public static void write(Object object, Sink sink) {
        if (object == null) {
            writeNull(sink);
            return;
        }
        if (object instanceof String value) {
            writeString(value, sink);
            return;
        }
        if (object instanceof BigDecimal bigDecimal) {
            writeDecimal(bigDecimal, sink);
            return;
        }
        if (object instanceof BigInteger bigInteger) {
            writeInteger(bigInteger, sink);
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
        if (object instanceof Optional<?> optional) {
            optional.ifPresentOrElse(
                value -> write(value, sink),
                () -> writeNull(sink)
            );
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
        if (object instanceof Map<?, ?> map) {
            writeObject(map, sink);
            return;
        }
        if (object instanceof List<?> list) {
            writeList(list, sink);
            return;
        }
        if (object instanceof Set<?> set) {
            writeSet(set, sink);
            return;
        }
        if (object instanceof Collection<?> collection) {
            writeCollection(collection, sink);
            return;
        }
        if (object instanceof Iterable<?> iterable) {
            writeIterable(iterable, sink);
            return;
        }
        if (object instanceof Stream<?> stream) {
            writeList(stream.toList(), sink);
        }
        writeString(object.toString(), sink);
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
        sink.accept("null");
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
        for (Map.Entry<?, ?> value: map.entrySet()) {
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
