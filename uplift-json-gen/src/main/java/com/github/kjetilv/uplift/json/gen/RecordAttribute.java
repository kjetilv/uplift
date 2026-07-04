package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;

import static com.github.kjetilv.uplift.json.gen.GenUtils.fieldName;
import static com.github.kjetilv.uplift.json.gen.GenUtils.packageOf;

record RecordAttribute(
    BaseType baseType,
    String callbackEvent,
    RecordComponentElement attribute,
    Variant variant,
    TypeMirror internalType
) {

    String fieldEvent() {
        if (baseType != null) {
            return baseType.fieldEventType().getName();
        }
        if (variant == Variant.GENERATED || variant == Variant.GENERATED_LIST) {
            return "object";
        }
        if (variant == Variant.ENUM || variant == Variant.ENUM_LIST) {
            return "string";
        }
        throw new IllegalStateException("Unsupported attribute type: " + this);
    }

    boolean requiresConversion() {
        return baseType != null && baseType().requiresConversion()
               || variant == Variant.ENUM
               || variant == Variant.ENUM_LIST;
    }

    String callbackHandler(TypeElement typeElement) {
        return "on" + callbackEvent + "(" +
               quote(fieldName(attribute)) +
               ", " + variant.midTerm(attribute, realType())
                   .map(term -> term + ", ")
                   .orElse("") +
               variant.callbackHandler(
                   typeElement,
                   attribute,
                   realType()
               ) + ")";
    }

    TypeMirror realType() {
        return internalType == null ? attribute.asType() : internalType;
    }

    boolean isGenerated() {
        return variant() == Variant.GENERATED_LIST || variant() == Variant.GENERATED;
    }

    private String writerClass() {
        return writerClass(attribute.asType().toString());
    }

    private String writerClass(String name) {
        var packageElement = packageOf(attribute);
        var prefix = packageElement.toString();
        return name.substring(prefix.length() + 1)
                   .replace('.', '_') + "_Writer";
    }

    private Optional<RecordComponentElement> enumType(
        RecordComponentElement element
    ) {
        return Optional.empty();
    }

    private static final String QUO = "\"";

    private static String quote(Object string) {
        return QUO + string + QUO;
    }
}
