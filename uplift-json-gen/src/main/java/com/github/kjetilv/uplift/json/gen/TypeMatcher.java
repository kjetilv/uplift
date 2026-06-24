package com.github.kjetilv.uplift.json.gen;

import module java.base;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

final class TypeMatcher {

    private final GenUtils genUtils;

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
        GenUtils genUtils
    ) {
        this.genUtils = genUtils;
        this.baseType = baseType;
        this.type = type;
        this.primitiveType = primitiveType;
        this.typeMirror = typeMirror;
        this.primitiveTypeMirror = primitiveTypeMirror;
    }

    Optional<RecordAttribute> recordAttribute(TypeMirror type, RecordComponentElement element, boolean list) {
        if (matches(type)) {
            if (primitiveType == null && baseType == null) {
                return Optional.of(
                    new RecordAttribute(
                        baseType,
                        "Object",
                        element,
                        list ? Variant.GENERATED_LIST : Variant.GENERATED
                    )
                );
            }
            if (genUtils.isMap(element)) {
                return Optional.of(
                    new RecordAttribute(
                        baseType,
                        "Object",
                        element,
                        Variant.GENERIC_MAP
                    )
                );
            }
            return Optional.of(
                new RecordAttribute(
                    baseType,
                    this.type.getSimpleName(),
                    element,
                    list ? Variant.PRIMITIVE_LIST : Variant.PRIMITIVE
                )
            );
        }
        return Optional.empty();
    }

    Optional<Found> match(TypeMirror type, RecordComponentElement element, boolean list) {
        if (matches(type)) {
            if (primitiveType == null && baseType == null) {
                return Optional.of(
                    new Found(this, element));
            }
            return Optional.of(
                new Found(this, element)
            );
        }
        return Optional.empty();
    }

    private boolean matches(TypeMirror type) {
        return genUtils.isSameType(type, typeMirror) ||
               primitiveType != null && genUtils.isSameType(type, primitiveTypeMirror);
    }

    record Found(TypeMatcher matcher, RecordComponentElement element) {
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               type.getSimpleName() + "/" + (primitiveType == null ? "" : primitiveType.getSimpleName()) +
               "]";
    }
}
