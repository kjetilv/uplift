package com.github.kjetilv.uplift.json;

import java.util.Map;

public class MapWriter implements ObjectWriter<Map<?, ?>> {

    @Override
    public FieldEvents write(Map<?, ?> object, FieldEvents calls) {
        object.forEach((key, value) ->
            resolve(key, value, calls));
        calls.done();
        return calls;
    }

    private static void resolve(Object key, Object value, FieldEvents calls) {
        switch (value) {
            case String string -> calls.string(key.toString(), string);
            case Number number -> calls.number(key.toString(), number);
            case Boolean bool -> calls.bool(key.toString(), bool);
            case Map<?, ?> submap -> new MapWriter().write(submap, calls);
            default -> throw new IllegalStateException("Not supported " + key + " -> " + value);
        }
    }
}
