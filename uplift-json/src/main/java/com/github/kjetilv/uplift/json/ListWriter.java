package com.github.kjetilv.uplift.json;

import java.util.List;
import java.util.Map;

public class ListWriter implements ObjectWriter<List<?>> {
    @Override
    public WriteEvents write(List<?> object, WriteEvents calls) {
        object.forEach(value ->
            resolve(value, calls));
        calls.done();
        return calls;

    }

    private static void resolve(Object value, WriteEvents calls) {
        switch (value) {
            case String string -> calls.stringField(key.toString(), string);
            case Number number -> calls.numberField(key.toString(), number);
            case Boolean bool -> calls.boolField(key.toString(), bool);
            case Map<?, ?> submap -> new MapWriter().write(submap, calls);
            case List<?> sublist -> new ListWriter().write(sublist, calls);
            default -> throw new IllegalStateException("Not supported " + key + " -> " + value);
        }
    }
}
