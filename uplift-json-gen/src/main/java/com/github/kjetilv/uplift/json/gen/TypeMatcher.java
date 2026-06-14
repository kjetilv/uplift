package com.github.kjetilv.uplift.json.gen;

import module java.base;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

final class TypeMatcher {

    private final Types typeUtils;

    private final BaseType baseType;

    private final Class<?> type;

    private final Class<?> primitiveType;

    private final DeclaredType typeMirror;

    private final TypeMirror primitiveTypeMirror;

    TypeMatcher(
        BaseType baseType,
        Class<?> type,
        Class<?> primitiveType,
        DeclaredType typeMirror,
        TypeMirror primitiveTypeMirror,
        Types typeUtils
    ) {
        this.typeUtils = typeUtils;
        this.baseType = baseType;
        this.type = type;
        this.primitiveType = primitiveType;
        this.typeMirror = typeMirror;
        this.primitiveTypeMirror = primitiveTypeMirror;
    }

    Optional<RecordAttribute> recordAttribute(RecordComponentElement recordAttribute) {
        return Optional.of(recordAttribute)
            .filter(attribute -> matches(attribute.asType()))
            .map(this::resolved);
    }

    private boolean matches(TypeMirror type) {
        return typeUtils.isSameType(type, typeMirror) ||
               primitiveType != null && typeUtils.isSameType(type, primitiveTypeMirror);
    }

    private RecordAttribute resolved(RecordComponentElement element) {
        return primitiveType == null && baseType == null
            ? new RecordAttribute(baseType, "Object", element, Variant.GENERATED)
            : new RecordAttribute(baseType, type.getSimpleName(), element, Variant.PRIMITIVE);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               type.getSimpleName() + "/" + (primitiveType == null ? "" : primitiveType.getSimpleName()) +
               "]";
    }
}
