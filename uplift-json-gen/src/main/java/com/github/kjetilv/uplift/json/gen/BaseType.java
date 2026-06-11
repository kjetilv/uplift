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

    static BaseType of(RecordComponentElement typeElement) {
        try {
            return pick(
                typeElement,
                baseType ->
                    isFor(baseType, typeElement)
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("No basetype for element " + typeElement, e);
        }
    }

    static BaseType of(String name) {
        try {
            return pick(
                name, baseType ->
                    baseType.fieldTypes.stream().anyMatch(fieldType -> fieldType.getName().equals(
                        name))
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("No basetype for type named " + name, e);
        }
    }

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

    public List<Class<?>> fieldTypes() {
        return fieldTypes;
    }

    public String typeName() {
        return switch (this) {
            case STRING, CHAR, UUID, URI, URL, INSTANT, DURATION, LOCALDATE, LOCALDATETIME, OFFSETDATETIME -> "string";
            case BOOLEAN -> "boolean";
            case INTEGER, LONG, SHORT, BYTE, BIG_INTEGER -> "integer";
            case DOUBLE, FLOAT, BIG_DECIMAL -> "number";
            case MAP -> "object";
        };
    }

    public String methodName() {
        return fieldEventType.getName();
    }

    public boolean requiresConversion() {
        return !(fieldTypes.contains(String.class) ||
                 fieldTypes.contains(Boolean.class) ||
                 canBeNumberic());
    }

    private boolean canBeNumberic() {
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

    private static boolean isFor(BaseType type, RecordComponentElement typeElement) {
        var typeElementName = typeElement.asType().toString();
        return type.fieldTypes.stream()
            .map(Class::getName)
            .anyMatch(typeElementName::equals);
    }
}
