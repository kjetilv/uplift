package com.github.kjetilv.uplift.json;

import com.github.kjetilv.uplift.json.io.Canonical;
import com.github.kjetilv.uplift.uuid.Uuid;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@SuppressWarnings({"MethodMayBeStatic", "unused"})
public abstract class AbstractObjectWriter<T extends Record> implements ObjectWriter<T> {

    private static final Pattern QUOTE = Pattern.compile("\"");

    @Override
    public final WriteEvents write(T object, WriteEvents events) {
        try {
            return doWrite(object, events);
        } finally {
            events.done();
        }
    }

    protected abstract WriteEvents doWrite(T object, WriteEvents events);

    protected String value(String value) {
        String contents = value.indexOf('"') >= 0
            ? QUOTE.matcher(value).replaceAll("\\\\\"")
            : value;
        return "\"%s\"".formatted(contents);
    }

    protected String value(Boolean value) {
        return value ? Canonical.TRUE : Canonical.FALSE;
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
}
