package com.github.kjetilv.uplift.json.io;

import module java.base;
import module uplift.json;

public class DefaultFieldEvents extends AbstractFieldEvents {

    private final Sink.Mark mark;

    public DefaultFieldEvents(FieldEvents parent, Sink sink) {
        super(parent, sink);
        sink().accept("{");
        this.mark = sink().mark();
    }

    @Override
    public FieldEvents map(String field, Map<?, ?> value, ObjectWriter<Map<?, ?>> writer) {
        return writeField(
            field,
            value,
            Function.identity(),
            map ->
                writer.write(map, new DefaultFieldEvents(this, sink()))
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
            _ ->
                writer.write(value, new DefaultFieldEvents(this, sink()))
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
            t -> writer.write(t, new DefaultFieldEvents(this, sink()))
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
        return writeField(field, value, toNumber, this::value);
    }

    @Override
    public <T> FieldEvents numberArray(String field, List<? extends T> values, Function<T, Number> toNumber) {
        return writeArray(field, values, toNumber, this::value);
    }

    @Override
    public <T> FieldEvents bool(String field, T value, Function<T, Boolean> toBool) {
        return writeField(field, value, toBool, this::value);
    }

    @Override
    public <T> FieldEvents boolArray(String field, List<? extends T> value, Function<T, Boolean> toBool) {
        return writeArray(field, value, toBool, this::value);
    }

    @Override
    public void done() {
        sink().accept("}");
    }

    protected <T, V> FieldEvents writeArray(
        String field,
        List<? extends T> values,
        Function<T, V> map,
        Consumer<V> setter
    ) {
        if (values == null || values.isEmpty()) {
            return this;
        }
        if (mark.moved()) {
            sink().accept(",");
        }
        field(field);
        sink().accept("[");
        var arrayMark = sink().mark();
        try {
            for (var value : values) {
                if (arrayMark.moved()) {
                    sink().accept(",");
                }
                setter.accept(map.apply(value));
            }
        } finally {
            sink().accept("]");
        }
        return this;
    }

    private <T, R> FieldEvents writeField(String field, T value, Function<T, R> writer, Consumer<R> setter) {
        if (value == null) {
            return this;
        }
        if (mark.moved()) {
            sink().accept(",");
        }
        field(field);
        setter.accept(writer.apply(value));
        return this;
    }
}
