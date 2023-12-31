package com.github.kjetilv.uplift.json.annpro;

import javax.lang.model.element.RecordComponentElement;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

enum BaseType {

    STRING(String.class),
    BOOLEAN(List.of(Boolean.class, Boolean.TYPE), Boolean.class),
    INTEGER(List.of(Integer.class, Integer.TYPE), Number.class),
    LONG(List.of(Long.class, Long.TYPE), Number.class),
    DOUBLE(List.of(Double.class, Double.TYPE), Number.class),
    FLOAT(List.of(Float.class, Float.TYPE), Number.class),
    SHORT(List.of(Short.class, Short.TYPE), Number.class),
    BYTE(List.of(Byte.class, Byte.TYPE), Number.class),
    BIG_DECIMAL(BigDecimal.class, Number.class),
    BIG_INTEGER(BigInteger.class, Number.class),
    UUID(java.util.UUID.class, String.class),
    Uuid(com.github.kjetilv.uplift.uuid.Uuid.class, String.class),
    INSTANT(Instant.class, Number.class),
    DURATION(Duration.class, String.class);

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

    BaseType(Class<?> fieldType) {
        this(fieldType, fieldType);
    }

    BaseType(Class<?> fieldType, Class<?> jsonType) {
        this(List.of(fieldType), jsonType);
    }

    BaseType(List<Class<?>> fieldTypes, Class<?> jsonType) {
        this.fieldTypes = fieldTypes;
        this.jsonType = jsonType;
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
        return !fieldTypes.contains(String.class) &&
               !fieldTypes.contains(Boolean.class) &&
               !canBeNumberic();
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
                new IllegalArgumentException("Unsupported: " + name));
    }

    private static boolean isFor(BaseType type, RecordComponentElement typeElement) {
        return isFor(type, typeElement.asType().toString());
    }

    private static boolean isFor(BaseType type, String string) {
        return type.fieldTypes.stream().anyMatch(fieldType -> fieldType.getName().equals(string));
    }
}
