package com.github.kjetilv.uplift.json.io;

import com.github.kjetilv.uplift.json.FieldEvents;
import com.github.kjetilv.uplift.uuid.Uuid;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public abstract class AbstractFieldEvents implements FieldEvents {

    private final FieldEvents parent;

    private final Sink sink;

    public AbstractFieldEvents(FieldEvents parent, Sink sink) {
        this.parent = parent;
        this.sink = sink;
    }

    protected void field(String field) {
        sink.accept("\"%s\":".formatted(field));
    }

    protected void value(String value) {
        boolean quoted = value.indexOf('"') >= 0;
        String formatted = quoted
            ? QUOTE.matcher(value).replaceAll("\\\\\"")
            : value;
        sink.accept("\"%s\"".formatted(formatted));
    }

    protected void value(Number value) {
        sink.accept(value);
    }

    protected void value(Boolean value) {
        sink.accept(value);
    }

    protected final Sink sink() {
        return sink;
    }

    protected final FieldEvents parent() {
        return parent;
    }

    private static final Pattern QUOTE = Pattern.compile("\"");

    protected static String boolValue(Boolean value) {
        return value ? Canonical.TRUE : Canonical.FALSE;
    }

    protected static String numberValue(Number value) {
        return value.toString();
    }

    protected static String enumValue(Enum<?> value) {
        return value.name();
    }

    protected static String UUIDValue(UUID value) {
        return value.toString();
    }

    protected static String uuidValue(Uuid value) {
        return value.digest();
    }

    protected static long instantValue(Instant value) {
        return value.toEpochMilli();
    }

    protected static String durationValue(Duration value) {
        return value.toString();
    }
}
