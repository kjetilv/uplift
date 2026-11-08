package com.github.kjetilv.uplift.json.io;

import module java.base;
import com.github.kjetilv.uplift.hash.Hash;

public final class JsonWrites {

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
            case Hash<?> hash -> writeString(sink, hash.digest());
            case UUID uuid -> writeString(sink, uuid.toString());
            case Instant instant -> sink.accept(instant.toEpochMilli());
            case Duration duration -> writeString(sink, duration.toString());
            case Map<?, ?> map -> writeObject(sink, map);
            case List<?> list -> writeList(sink, list);
            case Set<?> set -> writeSet(sink, set);
            case Collection<?> coll -> writeCollection(sink, coll);
            case Iterable<?> it -> writeIterable(sink, it);
            case Stream<?> str -> writeList(sink, str.toList());
            default -> writeString(sink, object.toString());
        }
    }

    /// Whether a value counts as JSON null: null, an empty optional, or a map with no entries other than
    /// ones that count as null. Map entries with such values are left out, and such maps are written as
    /// `null`. In lists, they are written as `null`, to keep the indexes of the other elements.
    ///
    /// @param value Value
    /// @return True if the value counts as null
    public static boolean writesNull(Object value) {
        return switch (value) {
            case null -> true;
            case Optional<?> optional -> optional.isEmpty() || writesNull(optional.get());
            case Map<?, ?> map -> noValues(map);
            default -> false;
        };
    }

    private JsonWrites() {
    }

    private static boolean noValues(Map<?, ?> map) {
        for (var value : map.values()) {
            if (!writesNull(value)) {
                return false;
            }
        }
        return true;
    }

    private static void writeString(Sink sink, String value) {
        sink.accept('"');
        sink.acceptEscaped(value);
        sink.accept('"');
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

    /// Writes the entries that do not [count as null][#writesNull(Object)]. With no such entries, the map
    /// itself is written as `null`.
    private static void writeObject(Sink sink, Map<?, ?> map) {
        var first = true;
        for (var entry : map.entrySet()) {
            var value = entry.getValue();
            if (writesNull(value)) {
                continue;
            }
            sink.accept(first ? '{' : ',');
            first = false;
            sink.accept('"');
            sink.acceptEscaped(String.valueOf(entry.getKey()));
            sink.accept("\":");
            write(sink, value);
        }
        if (first) {
            writeNull(sink);
        } else {
            sink.accept('}');
        }
    }

    private static void writeIterable(Sink sink, Iterable<?> iterable) {
        var iterator = iterable.iterator();
        if (iterator.hasNext()) {
            writeNonEmptyArray(sink, iterator);
        } else {
            sink.accept("[]");
        }
    }

    private static void writeList(Sink sink, List<?> list) {
        var iterator = list.iterator();
        if (iterator.hasNext()) {
            writeNonEmptyArray(sink, iterator);
        } else {
            sink.accept("[]");
        }
    }

    private static void writeSet(Sink sink, Set<?> list) {
        var iterator = list.iterator();
        if (iterator.hasNext()) {
            writeNonEmptyArray(sink, iterator);
        } else {
            sink.accept("[]");
        }
    }

    private static void writeCollection(Sink sink, Collection<?> collection) {
        var iterator = collection.iterator();
        if (iterator.hasNext()) {
            writeNonEmptyArray(sink, iterator);
        } else {
            sink.accept("[]");
        }
    }

    private static void writeNonEmptyArray(Sink sink, Iterator<?> iterator) {
        sink.accept('[');
        var first = true;
        while (iterator.hasNext()) {
            if (first) {
                first = false;
            } else {
                sink.accept(',');
            }
            write(sink, iterator.next());
        }
        sink.accept(']');
    }
}
