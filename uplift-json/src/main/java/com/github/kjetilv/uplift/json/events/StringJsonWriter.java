package com.github.kjetilv.uplift.json.events;

import module uplift.json;

public class StringJsonWriter<T extends Record> extends AbstractJsonWriter<T, StringBuilder, String> {

    public StringJsonWriter(ObjectWriter<T> objectWriter) {
        super(objectWriter);
    }

    @Override
    protected StringBuilder builder() {
        return new StringBuilder();
    }

    @Override
    protected Sink output(StringBuilder out) {
        return new StringSink(out);
    }

    @Override
    protected String result(StringBuilder out) {
        return out.toString();
    }
}
