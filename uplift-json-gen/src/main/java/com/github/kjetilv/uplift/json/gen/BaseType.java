package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;

enum BaseType {

    STRING(String.class, String.class),
    BOOLEAN(Boolean.class, Boolean.class, Boolean.TYPE),
    INTEGER(Number.class, Integer.class, Integer.TYPE),
    LONG(Number.class, Long.class, Long.TYPE),
    DOUBLE(Number.class, Double.class, Double.TYPE),
    FLOAT(Number.class, Float.class, Float.TYPE),
    SHORT(Number.class, Short.class, Short.TYPE),
    BYTE(Number.class, Byte.class, Byte.TYPE),
    BIG_DECIMAL(Number.class, BigDecimal.class),
    BIG_INTEGER(Number.class, BigInteger.class),
    UUID(String.class, UUID.class),
    URI(String.class, URI.class),
    URL(String.class, URL.class),
    INSTANT(Number.class, Instant.class),
    DURATION(String.class, Duration.class),
    LOCALDATE(String.class, LocalDate.class),
    LOCALDATETIME(String.class, LocalDateTime.class),
    OFFSETDATETIME(String.class, OffsetDateTime.class),
    MAP(Map.class, Map.class);

    static BaseType of(RecordComponentElement typeElement) {
        try {
            return pick(typeElement, baseType -> isFor(baseType, typeElement));
        } catch (Exception e) {
            throw new IllegalArgumentException("No basetype for element " + typeElement, e);
        }
    }

    static BaseType of(String name) {
        try {
            return pick(name, baseType -> isFor(baseType, name));
        } catch (Exception e) {
            throw new IllegalArgumentException("No basetype for type named " + name, e);
        }
    }

    private final List<Class<?>> fieldTypes;

    private final Class<?> jsonType;

    BaseType(Class<?> jsonType, Class<?>... fieldTypes) {
        this.jsonType = Objects.requireNonNull(jsonType, "jsonType");
        this.fieldTypes = List.of(fieldTypes);
        if (this.fieldTypes.isEmpty()) {
            throw new IllegalStateException("Execpted 1+ field types");
        }
    }

    public List<Class<?>> fieldTypes() {
        return fieldTypes;
    }

    public String methodName() {
        if (Number.class.isAssignableFrom(jsonType)) {
            return "number";
        }
        if (jsonType == String.class) {
            return "string";
        }
        if (jsonType == Boolean.class) {
            return "bool";
        }
        throw new IllegalStateException("No method name: " + this);
    }

    public boolean requiresConversion() {
        return !(
            fieldTypes.contains(String.class) ||
            fieldTypes.contains(Boolean.class) ||
            canBeNumberic()
        );
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
        return isFor(type, typeElement.asType().toString());
    }

    private static boolean isFor(BaseType type, String string) {
        return type.fieldTypes.stream()
            .anyMatch(fieldType ->
                fieldType.getName().equals(string));
    }
}
