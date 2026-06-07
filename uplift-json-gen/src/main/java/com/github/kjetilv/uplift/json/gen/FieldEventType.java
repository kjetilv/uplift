package com.github.kjetilv.uplift.json.gen;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public enum FieldEventType {

    STRING(String.class),
    BOOLEAN(Boolean.class, "bool"),
    NUMBER(Number.class),
    OBJECT(Map.class);

    private final Class<?> jsonType;

    private final String name;

    FieldEventType(Class<?> jsonType) {
        this(jsonType, null);
    }

    FieldEventType(Class<?> jsonType, String name) {
        this.jsonType = Objects.requireNonNull(jsonType, "jsonType");
        this.name = name == null ? name().toLowerCase(Locale.ROOT) : name;
    }

    public Class<?> getJsonType() {
        return jsonType;
    }

    public String getName() {
        return name;
    }

    String getArrayName() {
        return name + "Array";
    }
}
