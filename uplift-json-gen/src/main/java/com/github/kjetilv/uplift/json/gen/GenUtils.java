package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;
import com.github.kjetilv.uplift.json.anno.JsonRecord;

import javax.lang.model.type.TypeKind;

import static com.github.kjetilv.uplift.json.gen.RecordAttribute.Cardinality;
import static com.github.kjetilv.uplift.json.gen.RecordAttribute.Kind;
import static com.github.kjetilv.uplift.json.gen.SourceTypes.render;

/// Type analysis of record components
final class GenUtils {

    private final Types types;

    private final Elements elements;

    private final TypeMirror iterableErasure;

    private final TypeMirror listErasure;

    private final TypeMirror mapErasure;

    private final List<BaseTypeMirror> baseTypes;

    GenUtils(Types types, Elements elements) {
        this.types = Objects.requireNonNull(types, "types");
        this.elements = Objects.requireNonNull(elements, "elements");
        this.iterableErasure = erasure(Iterable.class);
        this.listErasure = erasure(List.class);
        this.mapErasure = erasure(Map.class);
        this.baseTypes = Arrays.stream(BaseType.values())
            .map(baseType ->
                new BaseTypeMirror(baseType, typeElement(baseType.type()).asType()))
            .toList();
    }

    RecordAttribute attribute(RecordComponentElement element) {
        var type = element.asType();
        if (type.getKind() == TypeKind.ERROR) {
            throw new GenException(element, "Unresolved type for " + element.getSimpleName());
        }
        if (type instanceof ArrayType arrayType) {
            return attribute(element, Cardinality.ARRAY, arrayType.getComponentType());
        }
        if (types.isAssignable(types.erasure(type), iterableErasure)) {
            return attribute(element, Cardinality.LIST, listElementType(element, type));
        }
        return attribute(element, Cardinality.SINGLE, type);
    }

    PackageElement packageOf(Element element) {
        return elements.getPackageOf(element);
    }

    TypeElement typeElement(TypeMirror type) {
        if (types.asElement(type) instanceof TypeElement typeElement) {
            return typeElement;
        }
        throw new IllegalArgumentException("Not a declared type: " + type);
    }

    /// @param type A type
    /// @return Source for the type, boxed if primitive
    String boxedName(TypeMirror type) {
        return type instanceof PrimitiveType primitiveType
            ? types.boxedClass(primitiveType).getQualifiedName().toString()
            : render(type);
    }

    String boxedName(TypeKind primitiveKind) {
        return boxedName(types.getPrimitiveType(primitiveKind));
    }

    private RecordAttribute attribute(
        RecordComponentElement element,
        Cardinality cardinality,
        TypeMirror componentType
    ) {
        var baseType = baseType(componentType);
        var kind = baseType.isPresent() ? Kind.VALUE : kind(element, componentType);
        if (kind == Kind.MAP && cardinality != Cardinality.SINGLE) {
            throw new GenException(element, "Lists and arrays of maps are not supported: " + element.getSimpleName());
        }
        return new RecordAttribute(element, kind, cardinality, componentType, baseType.orElse(null));
    }

    private TypeMirror listElementType(RecordComponentElement element, TypeMirror type) {
        if (!types.isSameType(types.erasure(type), listErasure)) {
            throw new GenException(
                element,
                "Unsupported collection type " + render(type) + " for " + element.getSimpleName() +
                ": only java.util.List is supported"
            );
        }
        return ((DeclaredType) type).getTypeArguments()
            .stream()
            .findFirst()
            .orElseThrow(() ->
                new GenException(element, "Raw list type not supported: " + element.getSimpleName()));
    }

    private Optional<BaseType> baseType(TypeMirror type) {
        return baseTypes.stream()
            .filter(baseTypeMirror ->
                baseTypeMirror.matches(type))
            .map(BaseTypeMirror::baseType)
            .findFirst();
    }

    private Kind kind(RecordComponentElement element, TypeMirror type) {
        if (type.getKind() == TypeKind.DECLARED) {
            if (types.isSameType(types.erasure(type), mapErasure)) {
                return Kind.MAP;
            }
            var typeElement = typeElement(type);
            if (typeElement.getKind() == ElementKind.ENUM) {
                return Kind.ENUM;
            }
            if (typeElement.getKind() == ElementKind.RECORD) {
                if (isGenerated(typeElement)) {
                    return Kind.RECORD;
                }
                throw new GenException(
                    element,
                    "Record type " + typeElement.getQualifiedName() + " of " + element.getSimpleName() +
                    " is not annotated with @" + JsonRecord.class.getSimpleName() +
                    ", and not nested in a record that is"
                );
            }
        }
        throw new GenException(element, "Unsupported type " + type + " for " + element.getSimpleName());
    }

    private TypeMirror erasure(Class<?> type) {
        return types.erasure(typeElement(type).asType());
    }

    private TypeElement typeElement(Class<?> type) {
        return Optional.ofNullable(elements.getTypeElement(type.getCanonicalName()))
            .orElseThrow(() ->
                new IllegalStateException(type + " not found"));
    }

    /// [JsonRecord] has source retention, so a record loaded from a class file shows no annotation.
    /// For such records, look for the generated callbacks class instead.
    ///
    /// @param typeElement A record
    /// @return True if the processor generates, or has generated, code for the record
    private boolean isGenerated(TypeElement typeElement) {
        return isAnnotated(typeElement) || elements.getTypeElement(
            Names.qualified(packageOf(typeElement), Names.flatName(typeElement) + "_Callbacks")
        ) != null;
    }

    /// @param typeElement A record
    /// @return True if the record, or a record it is nested in, is annotated, see [JsonRecordProcessor]
    private static boolean isAnnotated(TypeElement typeElement) {
        return Stream.iterate(
                (Element) typeElement,
                element -> element.getKind() == ElementKind.RECORD,
                Element::getEnclosingElement
            )
            .anyMatch(element ->
                element.getAnnotation(JsonRecord.class) != null);
    }

    private record BaseTypeMirror(BaseType baseType, TypeMirror type) {

        private boolean matches(TypeMirror typeMirror) {
            return baseType.isPrimitive(typeMirror) ||
                   typeMirror instanceof DeclaredType declared &&
                   type instanceof DeclaredType expected &&
                   declared.asElement().equals(expected.asElement());
        }
    }
}
