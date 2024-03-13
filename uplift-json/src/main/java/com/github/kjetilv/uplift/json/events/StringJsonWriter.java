package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.ObjectWriter;
import com.github.kjetilv.uplift.json.io.Sink;
import com.github.kjetilv.uplift.json.io.StringSink;

class StringJsonWriter<T extends Record, B> extends AbstractJsonWriter<T, StringBuilder, String> {

    StringJsonWriter(ObjectWriter<T> objectWriter) {
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
