package com.github.kjetilv.uplift.json.io;

import com.github.kjetilv.uplift.json.ObjectWriter;
import com.github.kjetilv.uplift.json.WriteEvents;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class DefaultWriteEvents extends AbstractWriteEvents {

    private final Sink.Mark mark;

    public DefaultWriteEvents(WriteEvents parent, Sink sink) {
        super(parent, sink);
        sink().accept("{");
        mark = sink().mark();
    }

    @Override
    public WriteEvents mapField(String field, Map<?, ?> value, ObjectWriter<Map<?, ?>> writer) {
        return writeField(
            field,
            value,
            Function.identity(),
            map ->
                writer.write(map, new DefaultWriteEvents(this, sink()))
        );
    }

    @Override
    public <T extends Record> WriteEvents objectField(
        String field,
        T value,
        ObjectWriter<T> writer
    ) {
        return writeField(
            field,
            value,
            Function.identity(),
            t ->
                writer.write(value, new DefaultWriteEvents(this, sink()))
        );
    }

    @Override
    public <T extends Record> WriteEvents objectArrayField(
        String field,
        List<? extends T> values,
        ObjectWriter<T> writer
    ) {
        return writeArray(
            field,
            values,
            Function.identity(),
            t -> writer.write(t, new DefaultWriteEvents(this, sink()))
        );
    }

    @Override
    public <T> WriteEvents stringField(String field, T value, Function<T, String> toString) {
        return writeField(field, value, toString, this::value);
    }

    @Override
    public <T> WriteEvents stringArrayField(String field, List<T> values, Function<T, String> toString) {
        return writeArray(field, values, toString, this::value);
    }

    @Override
    public <T> WriteEvents numberField(String field, T value, Function<T, Number> toNumber) {
        return writeField(field, value, toNumber, this::value);
    }

    @Override
    public <T> WriteEvents numberArrayField(String field, List<? extends T> values, Function<T, Number> toNumber) {
        return writeArray(field, values, toNumber, this::value);
    }

    @Override
    public <T> WriteEvents boolField(String field, T value, Function<T, Boolean> toBool) {
        return writeField(field, value, toBool, this::value);
    }

    @Override
    public <T> WriteEvents boolArrayField(String field, List<? extends T> value, Function<T, Boolean> toBool) {
        return writeArray(field, value, toBool, this::value);
    }

    @Override
    public void done() {
        sink().accept("}");
    }

    protected <T, V> WriteEvents writeArray(
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
        Sink.Mark arrayMark = sink().mark();
        try {
            for (T value : values) {
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

    private <T, R> WriteEvents writeField(String field, T value, Function<T, R> writer, Consumer<R> setter) {
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
