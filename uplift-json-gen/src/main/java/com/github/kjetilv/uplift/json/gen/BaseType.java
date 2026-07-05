package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;

enum BaseType {

    STRING(FieldEventType.STRING),
    MAP(FieldEventType.OBJECT),
    BOOLEAN(FieldEventType.BOOLEAN),
    INTEGER(FieldEventType.NUMBER),
    LONG(FieldEventType.NUMBER),
    CHAR(FieldEventType.NUMBER),
    DOUBLE(FieldEventType.NUMBER),
    FLOAT(FieldEventType.NUMBER),
    SHORT(FieldEventType.NUMBER),
    BYTE(FieldEventType.NUMBER),
    BIG_DECIMAL(FieldEventType.NUMBER),
    BIG_INTEGER(FieldEventType.NUMBER),
    UUID(FieldEventType.STRING, true),
    URI(FieldEventType.STRING, true),
    URL(FieldEventType.STRING, true),
    INSTANT(FieldEventType.NUMBER, true),
    DURATION(FieldEventType.STRING, true),
    LOCALDATE(FieldEventType.STRING, true),
    LOCALDATETIME(FieldEventType.STRING, true),
    OFFSETDATETIME(FieldEventType.STRING, true);

    private final FieldEventType fieldEventType;

    private final boolean convert;

    BaseType(FieldEventType fieldEventType) {
        this(fieldEventType, false);
    }

    BaseType(FieldEventType fieldEventType, boolean convert) {
        this.fieldEventType = fieldEventType;
        this.convert = convert;
    }

    public FieldEventType fieldEventType() {
        return fieldEventType;
    }

    public boolean requiresConversion() {
        return convert;
    }
}
