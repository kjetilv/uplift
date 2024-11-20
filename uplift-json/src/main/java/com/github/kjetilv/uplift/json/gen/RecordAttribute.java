package com.github.kjetilv.uplift.json.gen;

import com.github.kjetilv.uplift.json.MapCallbacks;
import com.github.kjetilv.uplift.json.MapWriter;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static com.github.kjetilv.uplift.json.gen.Gen.*;

record RecordAttribute(
    String callbackEvent,
    RecordComponentElement element,
    Variant variant,
    TypeElement internalType,
    Collection<? extends Element> roots,
    Collection<? extends Element> enums
) {

    static RecordAttribute create(
        RecordComponentElement element,
        Collection<? extends Element> roots,
        Collection<? extends Element> enums
    ) {
        return switch (element.asType().getKind()) {
            case BOOLEAN -> new RecordAttribute("Boolean", element, roots, enums);
            case BYTE -> new RecordAttribute("Byte", element, roots, enums);
            case SHORT -> new RecordAttribute("Short", element, roots, enums);
            case INT -> new RecordAttribute("Integer", element, roots, enums);
            case LONG -> new RecordAttribute("Long", element, roots, enums);
            case CHAR -> new RecordAttribute("Character", element, roots, enums);
            case FLOAT -> new RecordAttribute("Float", element, roots, enums);
            case DOUBLE -> new RecordAttribute("Double", element, roots, enums);
            case DECLARED -> resolveDeclared(element, roots, enums);
            default -> throw new IllegalStateException("Unsupported: " + element);
        };
    }

    RecordAttribute(
        String event,
        RecordComponentElement element,
        Collection<? extends Element> roots,
        Collection<? extends Element> enums
    ) {
        this(event, element, Variant.PRIMITIVE, null, roots, enums);
    }

    String callbackHandler(TypeElement builderType) {
        return "on" + callbackEvent + "(\"" +
               fieldName(element) + "\", " +
               variant.midTerm(element, internalType)
                   .map(term -> term + ", ").orElse("") +
               variant.callbackHandler(builderType, element, internalType) +
               ")";
    }

    String writeCall(TypeElement te) {
        Optional<String> listType = listType(element, roots, enums);
        boolean isEnum = isType(element, enums) || isListType(element, enums);
        boolean isRoot = isType(element, roots) || isListType(element, roots);
        boolean isMap = element.asType().toString().startsWith(Map.class.getName());
        boolean convert = !isMap && !isRoot && (isEnum || listType.map(BaseType::of)
                              .orElseGet(() -> BaseType.of(element))
                              .requiresConversion());
        String name = isRoot ? "object"
            : isMap ? "map"
                : isEnum ? "string"
                    : listType.map(BaseType::of).orElseGet(() -> BaseType.of(element)).methodName();
        return name +
               listType.map(_ -> "Array").orElse("") +
               "(\"" + element.getSimpleName() + "\", " +
               variableName(te) + "." + element.getSimpleName() + "()" +
               (convert ? ", this::value)"
                   : isRoot ? ", new " + listType.map(this::writerClass).orElseGet(() -> writerClass(element.asType()
                       .toString())) + "())"
                       : isMap ? ", new " + MapWriter.class.getName() + "())"
                           : ")");
    }

    private String writerClass(String name) {
        PackageElement packageElement = packageEl(element);
        String prefix = packageElement.toString();
        return packageElement + "." + name
            .substring(prefix.length() + 1)
            .replace('.', '_') + "_Writer";
    }

    private static RecordAttribute resolveDeclared(
        RecordComponentElement element,
        Collection<? extends Element> roots,
        Collection<? extends Element> enums
    ) {
        return enumType(element, enums).map(_ ->
                new RecordAttribute(
                    "Enum",
                    element,
                    Variant.ENUM,
                    null,
                    roots,
                    enums
                ))
            .or(() -> enumListType(element, enums).map(enumListType ->
                new RecordAttribute(
                    "Enum",
                    element,
                    Variant.ENUM_LIST,
                    (TypeElement) enumListType,
                    roots,
                    enums
                )))
            .or(() -> primitiveEvent(element).map(event ->
                new RecordAttribute(
                    event.getSimpleName(),
                    element,
                    Variant.PRIMITIVE,
                    null,
                    roots,
                    enums
                )))
            .or(() -> generatedEvent(element, roots).map(generatedType ->
                new RecordAttribute(
                    "Object",
                    element,
                    Variant.GENERATED,
                    generatedType,
                    roots,
                    enums
                )
            ))
            .or(() -> primitiveListType(element).map(primtiveType ->
                new RecordAttribute(
                    primtiveType.getSimpleName(),
                    element,
                    Variant.PRIMITIVE_LIST,
                    null,
                    roots,
                    enums
                )
            ))
            .or(() -> generatedListType(element, roots).map(generatedType ->
                new RecordAttribute(
                    "Object",
                    element,
                    Variant.GENERATED_LIST,
                    generatedType,
                    roots,
                    enums
                )
            ))
            .or(() -> Optional.of(element.asType().toString())
                .filter(mapType ->
                    mapType.startsWith("java.util.Map"))
                .map(_ ->
                    new RecordAttribute(
                        "Object",
                        element,
                        Variant.GENERIC_MAP,
                        null,
                        roots,
                        enums
                    ))
            )
            .orElseThrow(() ->
                new IllegalStateException("Unsupported element/type: " + element + "/" + element.asType() + ", roots are: " + roots));
    }

    enum Variant {
        PRIMITIVE() {
            @Override
            String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeElement generated) {
                return packageEl(builderType) + "." + builderClassPlain(builderType) + "::" + setter(element);
            }
        },
        PRIMITIVE_LIST() {
            @Override
            String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeElement generated) {
                return packageEl(builderType) + "." + builderClassPlain(builderType) + "::" + adder(element);
            }
        },
        GENERATED() {
            @Override
            String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeElement generated) {
                return "() -> " + packageEl(generated) + "." + callbacksClassPlain(generated) + ".create(this, builder()::" + setter(
                    element) + ")";
            }
        },
        GENERATED_LIST() {
            @Override
            String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeElement generated) {
                return "() -> " + packageEl(generated) + "." + callbacksClassPlain(generated) + ".create(this, builder()::" + adder(
                    element) + ")";
            }
        },
        GENERIC_MAP() {
            @Override
            String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeElement generated) {
                return "() -> " + MapCallbacks.class.getName() + ".create(this, builder()::" + setter(element) + ")";
            }
        },
        ENUM() {
            @Override
            Optional<String> midTerm(RecordComponentElement element, TypeElement internalType) {
                return Optional.of(element.asType().toString() + "::valueOf");
            }

            @Override
            String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeElement generated) {
                return builderClassPlain(builderType) + "::" + setter(element);
            }
        },
        ENUM_LIST {
            @Override
            Optional<String> midTerm(RecordComponentElement element, TypeElement internalType) {
                return Optional.of(internalType.asType().toString() + "::valueOf");
            }

            @Override
            String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeElement generated) {
                return builderClassPlain(builderType) + "::" + adder(element);
            }
        };

        Optional<String> midTerm(RecordComponentElement element, TypeElement internalType) {
            return Optional.empty();
        }

        abstract String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeElement generated);
    }
}
