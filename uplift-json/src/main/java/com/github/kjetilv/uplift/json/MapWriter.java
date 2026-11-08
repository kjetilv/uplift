package com.github.kjetilv.uplift.json;

import module java.base;

/// Writes a map as a JSON object. Strings, numbers, booleans and maps are written through the matching
/// [FieldEvents] methods. Other values are written by [FieldEvents#value(String, Object)].
///
/// Entries with values that
/// [count as null][com.github.kjetilv.uplift.json.io.JsonWrites#writesNull(Object)] are left out.
public final class MapWriter implements ObjectWriter<Map<?, ?>> {

    @Override
    public FieldEvents write(Map<?, ?> object, FieldEvents calls) {
        object.forEach((key, value) -> resolve(key, value, calls));
        calls.done();
        return calls;
    }

    private void resolve(Object key, Object value, FieldEvents calls) {
        var field = String.valueOf(key);
        switch (value) {
            case String string -> calls.string(field, string);
            case Number number -> calls.number(field, number);
            case Boolean bool -> calls.bool(field, bool);
            case Map<?, ?> map -> calls.map(field, map, this);
            case null, default -> calls.value(field, value);
        }
    }
}
