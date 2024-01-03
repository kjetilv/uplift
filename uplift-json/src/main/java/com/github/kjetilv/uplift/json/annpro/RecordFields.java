package com.github.kjetilv.uplift.json.annpro;

import com.github.kjetilv.uplift.json.anno.JsonRecord;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import java.util.Collection;
import java.util.Map;

public final class RecordFields {

    static RecordFields mapFields(Element type, Types typeUtils) {
        if (
            type instanceof TypeElement typeElement &&
            type.getAnnotation(JsonRecord.class) != null &&
            type.getEnclosingElement() instanceof PackageElement packageElement
        ) {
            return new RecordFields(typeElement, packageElement, RecordField.mapFields(type, typeUtils));
        } else {
            throw new IllegalStateException(
                "Only top-level records marked with " + JsonRecord.class.getName() + " allowed, got: " + type);
        }
    }

    private final TypeElement typeElement;

    private final PackageElement packageElement;

    private final Map<String, RecordField> fields;

    public RecordFields(TypeElement typeElement, PackageElement packageElement, Map<String, RecordField> fields) {
        this.typeElement = typeElement;
        this.packageElement = packageElement;
        this.fields = fields;
    }

    public RecordField get(String field) {
        RecordField recordField = fields.get(field);
        if (recordField == null) {
            throw new IllegalStateException(this + ": No field " + field);
        }
        return recordField;
    }

    public Collection<RecordField> values() {
        return fields.values();
    }

    public TypeElement typeElement() {
        return typeElement;
    }

    public PackageElement packageElement() {
        return packageElement;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + fields.keySet() + "]";
    }
}
