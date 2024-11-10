package com.github.kjetilv.uplift.json.gen;

import com.github.kjetilv.uplift.json.FieldEvents;
import com.github.kjetilv.uplift.json.ObjectWriter;
import com.github.kjetilv.uplift.uuid.Uuid;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@SuppressWarnings({"MethodMayBeStatic", "unused"})
public abstract class AbstractObjectWriter<T extends Record> implements ObjectWriter<T> {

    @Override
    public final FieldEvents write(T object, FieldEvents events) {
        try {
            return doWrite(object, events);
        } finally {
            events.done();
        }
    }

    protected String value(String value) {
        String contents = value.indexOf('"') >= 0
            ? QUOTE.matcher(value).replaceAll("\\\\\"")
            : value;
        return "\"%s\"".formatted(contents);
    }

    protected String value(Boolean value) {
        return value ? "true" : "false";
    }

    protected String value(Number value) {
        return value.toString();
    }

    protected String value(Enum<?> value) {
        return value.name();
    }

    protected String value(UUID value) {
        return value.toString();
    }

    protected String value(Uuid value) {
        return value.digest();
    }

    protected long value(Instant value) {
        return value.toEpochMilli();
    }

    protected String value(Duration value) {
        return value.toString();
    }

    protected abstract FieldEvents doWrite(T object, FieldEvents events);

    private static final Pattern QUOTE = Pattern.compile("\"");
}
