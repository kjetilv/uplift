package com.github.kjetilv.uplift.json;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
        if (object instanceof String value) {
            writeString(value, sink);
            return;
        }
        if (object instanceof BigDecimal bigDecimal) {
            writeDecimal(bigDecimal, sink);
            return;
        }
        if (object instanceof Number value) {
            sink.accept(value);
            return;
        }
        if (object instanceof Boolean value) {
            sink.accept(value);
            return;
        }
        if (object instanceof Map<?, ?> map) {
            writeObject(map, sink);
            return;
        }
        if (object instanceof List<?> list) {
            writeArray(list, sink);
            return;
        }
        if (object instanceof Collection<?> collection) {
            writeArray(collection, sink);
            return;
        }
        if (object instanceof Iterable<?> iterable) {
            writeArray(iterable, sink);
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
        String value = dec.toPlainString();
        sink.accept(value.startsWith("0.") ? value.substring(1) : value);
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

    private static void writeArray(Iterable<?> iterable, Sink sink) {
        Iterator<?> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            writeNonEmptyArray(sink, iterator);
        } else {
            sink.accept("[]");
        }
    }

    private static void writeArray(List<?> list, Sink sink) {
        Iterator<?> iterator = list.iterator();
        if (iterator.hasNext()) {
            writeNonEmptyArray(sink, iterator);
        } else {
            sink.accept("[]");
        }
    }

    private static void writeArray(Collection<?> collection, Sink sink) {
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

    private static String objectToString(Object object) {
        return object.getClass().getName() + "@" + Integer.toHexString(object.hashCode());
    }
}
