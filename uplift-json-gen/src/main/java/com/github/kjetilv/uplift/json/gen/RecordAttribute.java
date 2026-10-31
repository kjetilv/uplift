package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;
import com.github.kjetilv.uplift.json.anno.Field;
import com.github.kjetilv.uplift.json.anno.Singular;

import static com.github.kjetilv.uplift.json.gen.Names.upcased;

/// A record component, classified for code generation.
///
/// @param element Record component
/// @param kind What the component type, or the element type of a list or array, is
/// @param cardinality Single value, list or array
/// @param componentType The component type, or the element type of a list or array
/// @param baseType Base type, set only for [Kind#VALUE]
record RecordAttribute(
    RecordComponentElement element,
    Kind kind,
    Cardinality cardinality,
    TypeMirror componentType,
    BaseType baseType
) {

    RecordAttribute {
        Objects.requireNonNull(element, "element");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(cardinality, "cardinality");
        Objects.requireNonNull(componentType, "componentType");
        if ((kind == Kind.VALUE) != (baseType != null)) {
            throw new IllegalArgumentException("Base type must be set for values only: " + kind + "/" + baseType);
        }
    }

    /// @return Java identifier of the component
    String javaName() {
        return element.getSimpleName().toString();
    }

    /// @return Name of the JSON field
    String jsonName() {
        var field = element.getAnnotation(Field.class);
        return field == null ? javaName() : field.value();
    }

    /// @return Java identifier for a single element of a list or array
    String singularName() {
        var singular = element.getAnnotation(Singular.class);
        if (singular != null) {
            return singular.value();
        }
        var plural = javaName();
        return plural.endsWith("s") && plural.length() > 1
            ? plural.substring(0, plural.length() - 1)
            : plural;
    }

    String setter() {
        return "set" + upcased(javaName());
    }

    String adder() {
        return "add" + upcased(singularName());
    }

    /// @return Builder method that receives values from the parser
    String mutator() {
        return cardinality == Cardinality.SINGLE ? setter() : adder();
    }

    boolean isRecord() {
        return kind == Kind.RECORD;
    }

    boolean isPrimitiveArray() {
        return cardinality == Cardinality.ARRAY && componentType.getKind().isPrimitive();
    }

    /// @return Name of the [com.github.kjetilv.uplift.json.FieldEvents] method that writes the field
    String fieldEvent() {
        var event = switch (kind) {
            case VALUE -> baseType.fieldEventType().getName();
            case RECORD -> FieldEventType.OBJECT.getName();
            case ENUM -> FieldEventType.STRING.getName();
            case MAP -> "map";
        };
        return cardinality == Cardinality.SINGLE ? event : event + "Array";
    }

    /// @return True if the generated writer must convert values with `this::value`
    boolean requiresConversion() {
        return switch (kind) {
            case ENUM -> true;
            case VALUE -> baseType.requiresConversion() && !isPrimitiveArray();
            case RECORD, MAP -> false;
        };
    }

    /// @return Suffix of the `on...` method in [PresetCallbacksInitializer]
    String callbackEvent() {
        return switch (kind) {
            case VALUE -> baseType.callbackEvent();
            case ENUM -> "Enum";
            case RECORD, MAP -> "Object";
        };
    }

    enum Kind {
        VALUE,
        RECORD,
        ENUM,
        MAP
    }

    enum Cardinality {
        SINGLE,
        LIST,
        ARRAY
    }
}
