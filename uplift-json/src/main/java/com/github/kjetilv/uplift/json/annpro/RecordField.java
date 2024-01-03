package com.github.kjetilv.uplift.json.annpro;

import com.github.kjetilv.uplift.json.anno.Singular;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class RecordField {

    static Map<String, RecordField> mapFields(Element type, Types typeUtils) {
        return create(type, typeUtils)
            .stream()
            .collect(Collectors.toMap(
                RecordField::fieldName,
                Function.identity(),
                (rf1, rf2) -> {
                    throw new IllegalStateException("Merge: " + rf1 + "/" + rf2);
                },
                LinkedHashMap::new
            ));
    }

    private final RecordComponentElement field;

    private final PrimitiveType primitiveFieldTypeMirror;

    private final DeclaredType declaredFieldTypeMirror;

    private final List<? extends TypeMirror> genericFieldTypes;

    private final TypeElement type;

    public RecordField(
        RecordComponentElement field,
        PrimitiveType primitiveFieldTypeMirror,
        DeclaredType declaredFieldTypeMirror,
        List<? extends TypeMirror> genericFieldTypes,
        TypeElement type
    ) {
        this.field = Objects.requireNonNull(field, "field");
        this.primitiveFieldTypeMirror = primitiveFieldTypeMirror;
        this.declaredFieldTypeMirror = declaredFieldTypeMirror;
        if ((this.primitiveFieldTypeMirror == null) == (this.declaredFieldTypeMirror == null)) {
            throw new IllegalStateException(
                "Invalid types: " + this.primitiveFieldTypeMirror + " / " + this.declaredFieldTypeMirror);
        }
        this.genericFieldTypes = genericFieldTypes == null || genericFieldTypes.isEmpty()
            ? Collections.emptyList()
            : List.copyOf(genericFieldTypes);
        this.type = type;

        if (this.isCollection() && this.genericFieldTypes.size() != 1) {
            throw new IllegalArgumentException(this + " should have a generic type");
        }
    }

    public String fieldName() {
        return field.getSimpleName().toString();
    }

    public TypeMirror fieldTypeMirror() {
        return declaredFieldTypeMirror == null ? primitiveFieldTypeMirror : declaredFieldTypeMirror;
    }

    public RecordComponentElement component() {
        return field;
    }

    public TypeElement type() {
        return type;
    }

    public boolean isEnum() {
        return type.getKind() == ElementKind.ENUM;
    }

    public boolean isPrimitive() {
        return primitiveFieldTypeMirror != null;
    }

    public boolean isCollection() {
        return !isPrimitive() && type.getQualifiedName().toString().equals(List.class.getName());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + fieldName() + "]";
    }

    public String setter() {
        return "set" + upcased(fieldName());
    }

    public String adder() {
        if (isCollection()) {
            return "add" + upcased(singularName());
        }
        throw new IllegalStateException(this + " is not a list field");
    }

    public String singularName() {
        Singular annotation = field.getAnnotation(Singular.class);
        String plural = fieldName();
        return annotation != null ? annotation.value()
            : plural.endsWith("s") ? plural.substring(0, plural.length() - 1)
                : plural;
    }

    public TypeMirror collectionTypeMirror() {
        if (isCollection()) {
            return genericFieldTypes.getFirst();
        }
        throw new IllegalStateException(this + " is not a list field");
    }

    private static List<RecordField> create(Element recordType, Types typeUtils) {
        if (recordType instanceof TypeElement typeElement) {
            return typeElement.getRecordComponents()
                .stream()
                .map(element ->
                    create(element, typeUtils))
                .toList();
        }
        throw new IllegalArgumentException("Not a record type: " + recordType);
    }

    private static RecordField create(RecordComponentElement component, Types typeUtils) {
        PrimitiveType primitiveType = null;
        DeclaredType declaredType = null;
        List<? extends TypeMirror> genericTypes = null;
        TypeElement type = null;
        switch (component.asType()) {
            case DeclaredType declared -> {
                declaredType = declared;
                genericTypes = declaredType.getTypeArguments();
                type = (TypeElement) typeUtils.asElement(declared);
            }
            case PrimitiveType primitive -> primitiveType = primitive;
            default -> throw new IllegalStateException("Unsupported type: " + component);
        }
        return new RecordField(
            component,
            primitiveType,
            declaredType,
            genericTypes,
            type
        );
    }

    private static String upcased(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }
}
