package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;

import javax.lang.model.type.TypeKind;

/// Value types that map to a single JSON value
enum BaseType {

    STRING(String.class, FieldEventType.STRING),
    BOOLEAN(Boolean.class, TypeKind.BOOLEAN, FieldEventType.BOOLEAN),
    BYTE(Byte.class, TypeKind.BYTE, FieldEventType.NUMBER),
    SHORT(Short.class, TypeKind.SHORT, FieldEventType.NUMBER),
    INTEGER(Integer.class, TypeKind.INT, FieldEventType.NUMBER),
    LONG(Long.class, TypeKind.LONG, FieldEventType.NUMBER),
    CHAR(Character.class, TypeKind.CHAR, FieldEventType.STRING, true),
    FLOAT(Float.class, TypeKind.FLOAT, FieldEventType.NUMBER),
    DOUBLE(Double.class, TypeKind.DOUBLE, FieldEventType.NUMBER),
    BIG_DECIMAL(BigDecimal.class, FieldEventType.NUMBER),
    BIG_INTEGER(BigInteger.class, FieldEventType.NUMBER),
    INSTANT(Instant.class, FieldEventType.NUMBER, true),
    URL(URL.class, FieldEventType.STRING, true),
    URI(URI.class, FieldEventType.STRING, true),
    LOCALDATE(LocalDate.class, FieldEventType.STRING, true),
    LOCALDATETIME(LocalDateTime.class, FieldEventType.STRING, true),
    OFFSETDATETIME(OffsetDateTime.class, FieldEventType.STRING, true),
    DURATION(Duration.class, FieldEventType.STRING, true),
    UUID(UUID.class, FieldEventType.STRING, true);

    private final Class<?> type;

    private final TypeKind primitiveKind;

    private final FieldEventType fieldEventType;

    private final boolean convert;

    BaseType(Class<?> type, FieldEventType fieldEventType) {
        this(type, null, fieldEventType, false);
    }

    BaseType(Class<?> type, FieldEventType fieldEventType, boolean convert) {
        this(type, null, fieldEventType, convert);
    }

    BaseType(Class<?> type, TypeKind primitiveKind, FieldEventType fieldEventType) {
        this(type, primitiveKind, fieldEventType, false);
    }

    BaseType(Class<?> type, TypeKind primitiveKind, FieldEventType fieldEventType, boolean convert) {
        this.type = type;
        this.primitiveKind = primitiveKind;
        this.fieldEventType = fieldEventType;
        this.convert = convert;
    }

    Class<?> type() {
        return type;
    }

    boolean isPrimitive(TypeMirror typeMirror) {
        return primitiveKind != null && primitiveKind == typeMirror.getKind();
    }

    FieldEventType fieldEventType() {
        return fieldEventType;
    }

    /// @return True if the generated writer must convert the value with `this::value`
    boolean requiresConversion() {
        return convert;
    }

    /// @return Suffix of the `on...` method in [PresetCallbacksInitializer]
    String callbackEvent() {
        return type.getSimpleName();
    }
}
