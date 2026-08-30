package com.github.kjetilv.uplift.json.io;

import module java.base;
import com.github.kjetilv.uplift.json.FieldEvents;
import com.github.kjetilv.uplift.json.ObjectWriter;

@SuppressWarnings("DuplicatedCode")
public final class DefaultFieldEvents implements FieldEvents {

    private final Sink sink;

    private final Sink.Mark mark;

    public DefaultFieldEvents(Sink sink) {
        this.sink = Objects.requireNonNull(sink, "sink");
        this.sink.accept("{");
        this.mark = sink.mark();
    }

    @Override
    public FieldEvents string(String field, String value) {
        return writeField(field, value, sink::acceptQuoted);
    }

    @Override
    public FieldEvents number(String field, Number value) {
        return writeField(field, value, sink::accept);
    }

    @Override
    public FieldEvents bool(String field, Boolean value) {
        return writeField(field, value, sink::accept);
    }

    @Override
    public FieldEvents numberArray(String field, List<? extends Number> values) {
        return writeArray(field, values, sink::accept);
    }

    @Override
    public FieldEvents boolArray(String field, List<Boolean> values) {
        return writeArray(field, values, sink::accept);
    }

    @Override
    public FieldEvents numberArray(String field, double[] values) {
        return writeArray(field, values, sink::accept);
    }

    @Override
    public FieldEvents numberArray(String field, short[] values) {
        return writeArray(field, values, sink::accept);
    }

    @Override
    public FieldEvents numberArray(String field, Number[] values) {
        return writeArray(field, values, sink::accept);
    }

    @Override
    public <T> FieldEvents numberArray(String field, T[] values, Function<T, Number> toNumber) {
        return writeArray(field, values, sink::accept);
    }

    @Override
    public <T> FieldEvents boolArray(String field, T[] value, Function<T, Boolean> toBool) {
        return writeArray(field, value, sink::accept);
    }

    @Override
    public FieldEvents numberArray(String field, long[] values) {
        return writeArray(field, values, sink::accept);
    }

    @Override
    public FieldEvents numberArray(String field, int[] values) {
        return writeArray(field, values, sink::accept);
    }

    @Override
    public FieldEvents numberArray(String field, float[] values) {
        return writeArray(field, values, sink::accept);
    }

    @Override
    public FieldEvents numberArray(String field, byte[] values) {
        return writeArray(field, values, sink::accept);
    }

    @Override
    public FieldEvents boolArray(String field, boolean[] value) {
        return writeArray(field, value, sink::accept);
    }

    @Override
    public FieldEvents stringArray(String field, String[] values) {
        return writeArray(field, values, sink::acceptQuoted);
    }

    @Override
    public FieldEvents stringArray(String field, List<String> values) {
        return writeArray(field, values, sink::acceptQuoted);
    }

    @Override
    public <T extends Record> FieldEvents objectArray(String field, T[] values, ObjectWriter<T> writer) {
        return writeArray(
            field,
            values,
            value ->
                writer.write(value, newFieldEvents())
        );
    }

    @Override
    public FieldEvents map(String field, Map<?, ?> value, ObjectWriter<Map<?, ?>> writer) {
        return writeField(
            field,
            value,
            Function.identity(),
            map ->
                writer.write(map, newFieldEvents())
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
                writer.write(value, newFieldEvents())
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
            value ->
                writer.write(value, newFieldEvents())
        );
    }

    @Override
    public <T> FieldEvents string(String field, T value, Function<T, String> toString) {
        return writeField(field, value, toString, sink::acceptQuoted);
    }

    @Override
    public <T> FieldEvents stringArray(String field, List<T> values, Function<T, String> toString) {
        return writeArray(field, values, toString, sink::acceptQuoted);
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

    private <T> FieldEvents writeField(
        String field,
        T value,
        Consumer<T> setter
    ) {
        if (value == null) {
            return this;
        }
        if (mark.moved()) {
            sink.accept(",");
        }
        sink.accept(quoted(field));
        setter.accept(value);
        return this;
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
        prepareArray(field);
        boolean first = true;
        try {
            for (var value : values) {
                first = commaUnless(first);
                setter.accept(map.apply(value));
            }
        } finally {
            closeArray();
        }
        return this;
    }

    private <T, V> FieldEvents writeArray(
        String field,
        T[] values,
        Function<T, V> map,
        Consumer<V> setter
    ) {
        if (values == null || values.length == 0) {
            return this;
        }
        prepareArray(field);
        boolean first = true;
        try {
            for (var value : values) {
                first = commaUnless(first);
                setter.accept(map.apply(value));
            }
        } finally {
            closeArray();
        }
        return this;
    }

    private <T> FieldEvents writeArray(
        String field,
        T[] values,
        Consumer<T> setter
    ) {
        if (values == null || values.length == 0) {
            return this;
        }
        prepareArray(field);
        boolean first = true;
        try {
            for (var value : values) {
                if (first) {
                    first = false;
                } else {
                    sink.accept(",");
                }
                setter.accept(value);
            }
        } finally {
            closeArray();
        }
        return this;
    }

    private <T> FieldEvents writeArray(
        String field,
        List<? extends T> values,
        Consumer<T> setter
    ) {
        if (values == null || values.isEmpty()) {
            return this;
        }
        prepareArray(field);
        boolean first = true;
        try {
            for (var value : values) {
                if (first) {
                    first = false;
                } else {
                    sink.accept(",");
                }
                setter.accept(value);
            }
        } finally {
            closeArray();
        }
        return this;
    }

    private FieldEvents writeArray(
        String field,
        boolean[] values,
        Consumer<Boolean> setter
    ) {
        if (values == null || values.length == 0) {
            return this;
        }
        prepareArray(field);
        boolean first = true;
        try {
            for (var value : values) {
                first = commaUnless(first);
                setter.accept(value);
            }
        } finally {
            closeArray();
        }
        return this;
    }

    private FieldEvents writeArray(
        String field,
        int[] values,
        Consumer<Integer> setter
    ) {
        if (values == null || values.length == 0) {
            return this;
        }
        prepareArray(field);
        boolean first = true;
        try {
            for (var value : values) {
                first = commaUnless(first);
                setter.accept(value);
            }
        } finally {
            closeArray();
        }
        return this;
    }

    private FieldEvents writeArray(
        String field,
        long[] values,
        Consumer<Long> setter
    ) {
        if (values == null || values.length == 0) {
            return this;
        }
        prepareArray(field);
        boolean first = true;
        try {
            for (var value : values) {
                first = commaUnless(first);
                setter.accept(value);
            }
        } finally {
            closeArray();
        }
        return this;
    }

    private FieldEvents writeArray(
        String field,
        double[] values,
        Consumer<Double> setter
    ) {
        if (values == null || values.length == 0) {
            return this;
        }
        prepareArray(field);
        boolean first = true;
        try {
            for (var value : values) {
                first = commaUnless(first);
                setter.accept(value);
            }
        } finally {
            closeArray();
        }
        return this;
    }

    private FieldEvents writeArray(
        String field,
        float[] values,
        Consumer<Float> setter
    ) {
        if (values == null || values.length == 0) {
            return this;
        }
        prepareArray(field);
        boolean first = true;
        try {
            for (var value : values) {
                first = commaUnless(first);
                setter.accept(value);
            }
        } finally {
            closeArray();
        }
        return this;
    }

    private FieldEvents writeArray(
        String field,
        short[] values,
        Consumer<Short> setter
    ) {
        if (values == null || values.length == 0) {
            return this;
        }
        prepareArray(field);
        boolean first = true;
        try {
            for (var value : values) {
                first = commaUnless(first);
                setter.accept(value);
            }
        } finally {
            closeArray();
        }
        return this;
    }

    private FieldEvents writeArray(
        String field,
        byte[] values,
        Consumer<Byte> setter
    ) {
        if (values == null || values.length == 0) {
            return this;
        }
        prepareArray(field);
        boolean first = true;
        try {
            for (var value : values) {
                first = commaUnless(first);
                setter.accept(value);
            }
        } finally {
            closeArray();
        }
        return this;
    }

    private FieldEvents newFieldEvents() {
        return new DefaultFieldEvents(sink);
    }

    private void prepareArray(String field) {
        if (mark.moved()) {
            sink.accept(",");
        }
        sink.accept(quoted(field));
        sink.accept("[");
    }

    private boolean commaUnless(boolean first) {
        if (!first) {
            sink.accept(",");
        }
        return false;
    }

    private void closeArray() {
        sink.accept("]");
    }

    private static String quoted(String field) {
        return "\"%s\":".formatted(field);
    }

}
