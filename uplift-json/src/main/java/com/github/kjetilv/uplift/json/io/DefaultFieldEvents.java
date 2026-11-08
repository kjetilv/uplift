package com.github.kjetilv.uplift.json.io;

import module java.base;
import com.github.kjetilv.uplift.json.FieldEvents;
import com.github.kjetilv.uplift.json.ObjectWriter;

/// Writes the fields of one JSON object to a [Sink]. Null values and empty arrays are not written.
///
/// Not thread-safe. This class holds mutable state, since it is the inner loop of all object writes.
@SuppressWarnings("DuplicatedCode")
public final class DefaultFieldEvents implements FieldEvents {

    private final Sink sink;

    /// Whether a field has been written, so the next one needs a comma
    private boolean started;

    public DefaultFieldEvents(Sink sink) {
        this.sink = Objects.requireNonNull(sink, "sink");
        this.sink.accept('{');
    }

    @Override
    public FieldEvents string(String field, String value) {
        if (value != null) {
            field(field);
            sink.acceptQuoted(value);
        }
        return this;
    }

    @Override
    public FieldEvents number(String field, Number value) {
        if (value != null) {
            field(field);
            sink.accept(value);
        }
        return this;
    }

    @Override
    public FieldEvents bool(String field, Boolean value) {
        if (value != null) {
            field(field);
            sink.accept(value.booleanValue());
        }
        return this;
    }

    @Override
    public <T> FieldEvents string(String field, T value, Function<T, String> toString) {
        if (value != null) {
            field(field);
            sink.acceptQuoted(toString.apply(value));
        }
        return this;
    }

    @Override
    public <T> FieldEvents number(String field, T value, Function<T, Number> toNumber) {
        if (value != null) {
            field(field);
            sink.accept(toNumber.apply(value));
        }
        return this;
    }

    @Override
    public <T> FieldEvents bool(String field, T value, Function<T, Boolean> toBool) {
        if (value != null) {
            field(field);
            sink.accept(toBool.apply(value).booleanValue());
        }
        return this;
    }

    @Override
    public FieldEvents map(String field, Map<?, ?> value, ObjectWriter<Map<?, ?>> writer) {
        if (!JsonWrites.writesNull(value)) {
            field(field);
            writer.write(value, new DefaultFieldEvents(sink));
        }
        return this;
    }

    @Override
    public <T extends Record> FieldEvents object(String field, T value, ObjectWriter<T> writer) {
        if (value != null) {
            field(field);
            writer.write(value, new DefaultFieldEvents(sink));
        }
        return this;
    }

    @Override
    public FieldEvents value(String field, Object value) {
        if (!JsonWrites.writesNull(value)) {
            field(field);
            JsonWrites.write(sink, value);
        }
        return this;
    }

    @Override
    public FieldEvents stringArray(String field, List<String> values) {
        return writeArray(field, values, Sink::acceptQuoted);
    }

    @Override
    public FieldEvents stringArray(String field, String[] values) {
        return writeArray(field, values, Sink::acceptQuoted);
    }

    @Override
    public <T> FieldEvents stringArray(String field, List<T> values, Function<T, String> toString) {
        return writeArray(field, values, (sink, value) -> sink.acceptQuoted(toString.apply(value)));
    }

    @Override
    public <T> FieldEvents stringArray(String field, T[] values, Function<T, String> toString) {
        return writeArray(field, values, (sink, value) -> sink.acceptQuoted(toString.apply(value)));
    }

    @Override
    public FieldEvents numberArray(String field, List<? extends Number> values) {
        return writeArray(field, values, Sink::accept);
    }

    @Override
    public FieldEvents numberArray(String field, Number[] values) {
        return writeArray(field, values, Sink::accept);
    }

    @Override
    public <T> FieldEvents numberArray(String field, List<? extends T> values, Function<T, Number> toNumber) {
        return writeArray(field, values, (sink, value) -> sink.accept(toNumber.apply(value)));
    }

    @Override
    public <T> FieldEvents numberArray(String field, T[] values, Function<T, Number> toNumber) {
        return writeArray(field, values, (sink, value) -> sink.accept(toNumber.apply(value)));
    }

    @Override
    public FieldEvents boolArray(String field, List<Boolean> values) {
        return writeArray(field, values, DefaultFieldEvents::bool);
    }

    @Override
    public <T> FieldEvents boolArray(String field, List<? extends T> value, Function<T, Boolean> toBool) {
        return writeArray(field, value, (sink, item) -> bool(sink, toBool.apply(item)));
    }

    @Override
    public <T> FieldEvents boolArray(String field, T[] value, Function<T, Boolean> toBool) {
        return writeArray(field, value, (sink, item) -> bool(sink, toBool.apply(item)));
    }

    @Override
    public <T extends Record> FieldEvents objectArray(String field, List<? extends T> values, ObjectWriter<T> writer) {
        return writeArray(field, values, (sink, value) -> writer.write(value, new DefaultFieldEvents(sink)));
    }

    @Override
    public <T extends Record> FieldEvents objectArray(String field, T[] values, ObjectWriter<T> writer) {
        return writeArray(field, values, (sink, value) -> writer.write(value, new DefaultFieldEvents(sink)));
    }

    @Override
    public FieldEvents stringArray(String field, char[] values) {
        if (values == null || values.length == 0) {
            return this;
        }
        openArray(field);
        for (var i = 0; i < values.length; i++) {
            comma(i);
            sink.acceptQuoted(String.valueOf(values[i]));
        }
        return closeArray();
    }

    @Override
    public FieldEvents numberArray(String field, long[] values) {
        if (values == null || values.length == 0) {
            return this;
        }
        openArray(field);
        for (var i = 0; i < values.length; i++) {
            comma(i);
            sink.accept(values[i]);
        }
        return closeArray();
    }

    @Override
    public FieldEvents numberArray(String field, int[] values) {
        if (values == null || values.length == 0) {
            return this;
        }
        openArray(field);
        for (var i = 0; i < values.length; i++) {
            comma(i);
            sink.accept((long) values[i]);
        }
        return closeArray();
    }

    @Override
    public FieldEvents numberArray(String field, short[] values) {
        if (values == null || values.length == 0) {
            return this;
        }
        openArray(field);
        for (var i = 0; i < values.length; i++) {
            comma(i);
            sink.accept((long) values[i]);
        }
        return closeArray();
    }

    @Override
    public FieldEvents numberArray(String field, byte[] values) {
        if (values == null || values.length == 0) {
            return this;
        }
        openArray(field);
        for (var i = 0; i < values.length; i++) {
            comma(i);
            sink.accept((long) values[i]);
        }
        return closeArray();
    }

    @Override
    public FieldEvents numberArray(String field, double[] values) {
        if (values == null || values.length == 0) {
            return this;
        }
        openArray(field);
        for (var i = 0; i < values.length; i++) {
            comma(i);
            sink.accept(Double.toString(values[i]));
        }
        return closeArray();
    }

    @Override
    public FieldEvents numberArray(String field, float[] values) {
        if (values == null || values.length == 0) {
            return this;
        }
        openArray(field);
        for (var i = 0; i < values.length; i++) {
            comma(i);
            sink.accept(Float.toString(values[i]));
        }
        return closeArray();
    }

    @Override
    public FieldEvents boolArray(String field, boolean[] values) {
        if (values == null || values.length == 0) {
            return this;
        }
        openArray(field);
        for (var i = 0; i < values.length; i++) {
            comma(i);
            sink.accept(values[i]);
        }
        return closeArray();
    }

    @Override
    public void done() {
        sink.accept('}');
    }

    private <T> FieldEvents writeArray(String field, List<? extends T> values, BiConsumer<Sink, ? super T> writer) {
        if (values == null || values.isEmpty()) {
            return this;
        }
        openArray(field);
        var first = true;
        for (var value : values) {
            if (first) {
                first = false;
            } else {
                sink.accept(',');
            }
            writer.accept(sink, value);
        }
        return closeArray();
    }

    private <T> FieldEvents writeArray(String field, T[] values, BiConsumer<Sink, ? super T> writer) {
        if (values == null || values.length == 0) {
            return this;
        }
        openArray(field);
        for (var i = 0; i < values.length; i++) {
            comma(i);
            writer.accept(sink, values[i]);
        }
        return closeArray();
    }

    private void field(String field) {
        if (started) {
            sink.accept(',');
        } else {
            started = true;
        }
        sink.acceptField(field);
    }

    private void openArray(String field) {
        field(field);
        sink.accept('[');
    }

    private void comma(int index) {
        if (index > 0) {
            sink.accept(',');
        }
    }

    private FieldEvents closeArray() {
        sink.accept(']');
        return this;
    }

    /// Writes a boxed boolean as [Object#toString()] would, failing on null as before
    private static void bool(Sink sink, Boolean value) {
        sink.accept(value.booleanValue());
    }
}
