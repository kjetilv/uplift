package com.github.kjetilv.uplift.json.io;

import module java.base;
import com.github.kjetilv.uplift.json.FieldEvents;
import com.github.kjetilv.uplift.json.ObjectWriter;

public final class DefaultFieldEvents implements FieldEvents {

    private final Sink sink;

    private final Sink.Mark mark;

    public DefaultFieldEvents(Sink sink) {
        this.sink = Objects.requireNonNull(sink, "sink");
        this.sink.accept("{");
        this.mark = sink.mark();
    }

    @Override
    public FieldEvents map(String field, Map<?, ?> value, ObjectWriter<Map<?, ?>> writer) {
        return writeField(
            field,
            value,
            Function.identity(),
            map ->
                writer.write(map, fieldEvents())
        );
    }

    @Override
    public <T extends Record> FieldEvents object(
        String field,
        T value,
        ObjectWriter<T> writer
    ) {
        return writeField(
            field,
            value,
            Function.identity(),
            t ->
                writer.write(t, fieldEvents())
        );
    }

    @Override
    public <T extends Record> FieldEvents objectArray(
        String field,
        List<? extends T> values,
        ObjectWriter<T> writer
    ) {
        return writeArray(
            field,
            values,
            Function.identity(),
            t ->
                writer.write(t, fieldEvents())
        );
    }

    @Override
    public <T> FieldEvents string(String field, T value, Function<T, String> toString) {
        return writeField(field, value, toString, this::value);
    }

    @Override
    public <T> FieldEvents stringArray(String field, List<T> values, Function<T, String> toString) {
        return writeArray(field, values, toString, this::value);
    }

    @Override
    public <T> FieldEvents number(String field, T value, Function<T, Number> toNumber) {
        return writeField(field, value, toNumber, sink::accept);
    }

    @Override
    public <T> FieldEvents numberArray(String field, List<? extends T> values, Function<T, Number> toNumber) {
        return writeArray(field, values, toNumber, sink::accept);
    }

    @Override
    public <T> FieldEvents bool(String field, T value, Function<T, Boolean> toBool) {
        return writeField(field, value, toBool, sink::accept);
    }

    @Override
    public <T> FieldEvents boolArray(String field, List<? extends T> value, Function<T, Boolean> toBool) {
        return writeArray(field, value, toBool, sink::accept);
    }

    @Override
    public void done() {
        sink.accept("}");
    }

    private <T, R> FieldEvents writeField(
        String field,
        T value,
        Function<T, R> writer,
        Consumer<R> setter
    ) {
        if (value == null) {
            return this;
        }
        if (mark.moved()) {
            sink.accept(",");
        }
        sink.accept(quoted(field));
        setter.accept(writer.apply(value));
        return this;
    }

    private void value(String value) {
        var quoted = value.indexOf('"') >= 0;
        var unquoted = quoted
            ? replaceAll(value)
            : value;
        sink.accept("\"%s\"".formatted(unquoted));
    }

    private <T, V> FieldEvents writeArray(
        String field,
        List<? extends T> values,
        Function<T, V> map,
        Consumer<V> setter
    ) {
        if (values == null || values.isEmpty()) {
            return this;
        }
        if (mark.moved()) {
            sink.accept(",");
        }
        sink.accept(quoted(field));
        sink.accept("[");
        boolean first = true;
        try {
            for (var value : values) {
                if (first) {
                    first = false;
                } else {
                    sink.accept(",");
                }
                setter.accept(map.apply(value));
            }
        } finally {
            sink.accept("]");
        }
        return this;
    }

    private FieldEvents fieldEvents() {
        return new DefaultFieldEvents(sink);
    }

    private static final Pattern QUOTE = Pattern.compile("\"");

    private static String quoted(String field) {
        return "\"%s\":".formatted(field);
    }

    private static String replaceAll(String value) {
        try {
            return QUOTE.matcher(value).replaceAll("\\\\\"");
        } catch (Exception e) {
            throw new IllegalStateException("Could not replace `" + QUOTE.pattern() + "` with `\\\\`: " + value, e);
        }
    }
}
