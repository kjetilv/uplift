package com.github.kjetilv.uplift.json;

import java.util.Map;

public class MapWriter implements ObjectWriter<Map<?, ?>> {
    @Override
    public WriteEvents write(Map<?, ?> object, WriteEvents calls) {
        object.forEach((key, value) ->
            resolve(key, value, calls));
        calls.done();
        return calls;
    }

    private static void resolve(Object key, Object value, WriteEvents calls) {
        switch (value) {
            case String string -> calls.stringField(key.toString(), string);
            case Number number -> calls.numberField(key.toString(), number);
            case Boolean bool -> calls.boolField(key.toString(), bool);
            case Map<?, ?> submap -> new MapWriter().write(submap, calls);
            default -> throw new IllegalStateException("Not supported " + key + " -> " + value);
        };
    }
}
