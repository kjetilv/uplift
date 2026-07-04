package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;

enum BaseType {

    STRING(FieldEventType.STRING),
    MAP(FieldEventType.OBJECT),
    BOOLEAN(
        FieldEventType.BOOLEAN,
        Boolean.class, Boolean.TYPE
    ),
    INTEGER(
        FieldEventType.NUMBER,
        Integer.class, Integer.TYPE
    ),
    LONG(
        FieldEventType.NUMBER,
        Long.class, Long.TYPE
    ),
    CHAR(
        FieldEventType.NUMBER,
        Character.class, Character.TYPE
    ),
    DOUBLE(
        FieldEventType.NUMBER,
        Double.class, Double.TYPE
    ),
    FLOAT(
        FieldEventType.NUMBER,
        Float.class, Float.TYPE
    ),
    SHORT(
        FieldEventType.NUMBER,
        Short.class, Short.TYPE
    ),
    BYTE(
        FieldEventType.NUMBER,
        Byte.class, Byte.TYPE
    ),
    BIG_DECIMAL(
        FieldEventType.NUMBER,
        BigDecimal.class
    ),
    BIG_INTEGER(
        FieldEventType.NUMBER,
        BigInteger.class
    ),
    UUID(
        FieldEventType.STRING,
        java.util.UUID.class
    ),
    URI(
        FieldEventType.STRING,
        java.net.URI.class
    ),
    URL(
        FieldEventType.STRING,
        java.net.URL.class
    ),
    INSTANT(
        FieldEventType.NUMBER,
        Instant.class
    ),
    DURATION(
        FieldEventType.STRING,
        Duration.class
    ),
    LOCALDATE(
        FieldEventType.STRING,
        LocalDate.class
    ),
    LOCALDATETIME(
        FieldEventType.STRING,
        LocalDateTime.class
    ),
    OFFSETDATETIME(
        FieldEventType.STRING,
        OffsetDateTime.class
    );

    private final List<Class<?>> fieldTypes;

    private final FieldEventType fieldEventType;

    private final Class<?> jsonType;

    BaseType(FieldEventType fieldEventType) {
        this(fieldEventType, fieldEventType.getJsonType());
    }

    BaseType(FieldEventType fieldEventType, Class<?>... fieldTypes) {
        this.fieldEventType = fieldEventType;
        this.jsonType = fieldEventType.getJsonType();
        this.fieldTypes = Arrays.asList(fieldTypes);
    }

    public FieldEventType fieldEventType() {
        return fieldEventType;
    }

    public boolean requiresConversion() {
        return !(fieldTypes.contains(String.class) ||
                 fieldTypes.contains(Boolean.class) ||
                 canBeNumeric());
    }

    private boolean canBeNumeric() {
        return Number.class.isAssignableFrom(jsonType) &&
               fieldTypes.stream().anyMatch(Number.class::isAssignableFrom);
    }

    private static BaseType pick(Object name, Predicate<BaseType> baseTypePredicate) {
        return Arrays.stream(values())
            .filter(baseTypePredicate)
            .findFirst()
            .orElseThrow(() ->
                new IllegalArgumentException("Unsupported: " + name + " (" + name.getClass() + ")"));
    }

}
