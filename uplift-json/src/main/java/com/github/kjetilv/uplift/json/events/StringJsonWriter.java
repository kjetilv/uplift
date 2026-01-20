package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.ObjectWriter;
import com.github.kjetilv.uplift.json.io.Sink;

public class StringJsonWriter<T extends Record> extends AbstractJsonWriter<String, T, StringBuilder> {

    public StringJsonWriter(ObjectWriter<T> objectWriter) {
        super(objectWriter);
    }

    @Override
    protected StringBuilder builder() {
        return new StringBuilder();
    }

    @Override
    protected Sink output(StringBuilder out) {
        return Sink.string(out);
    }

    @Override
    protected String result(StringBuilder out) {
        return out.toString();
    }
}
