package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;
import com.github.kjetilv.uplift.json.MapWriter;

import static com.github.kjetilv.uplift.json.gen.GenUtils.*;

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
        return " on" + callbackEvent + "(" +
               quote(fieldName(element)) +
               ", " + variant.midTerm(element, internalType)
                   .map(term -> term + ", ")
                   .orElse("") +
               variant.callbackHandler(builderType, element, internalType) +
               ")";
    }

    boolean isGenerated() {
        return variant() == Variant.GENERATED_LIST || variant() == Variant.GENERATED;
    }

    String writeCall(TypeElement te) {
        var listType = listType(element, roots, enums);
        var isEnum = isType(element, enums) || isListType(element, enums);
        var isRoot = isType(element, roots) || isListType(element, roots);
        var isMap = element.asType().toString().startsWith(Map.class.getName());
        var convert = !isMap && !isRoot && (isEnum || listType.map(BaseType::of)
            .orElseGet(() -> BaseType.of(element))
            .requiresConversion());
        var name = isRoot ? "object"
            : isMap ? "map"
                : isEnum ? "string"
                    : listType.map(BaseType::of).orElseGet(() -> BaseType.of(element)).methodName();
        return name +
               listType.map(_ -> "Array").orElse("") +
               "(" +
               quote(element.getSimpleName()) +
               ", " +
               variableName(te) + "." + element.getSimpleName() + "()" +
               (convert ? ", this::value)"
                   : isRoot ? ", new " + listType.map(this::writerClass).orElseGet(this::writerClass) + "())"
                       : isMap ? ", new " + MapWriter.class.getName() + "())"
                           : ")");
    }

    private String writerClass() {
        return writerClass(element.asType().toString());
    }

    private String writerClass(String name) {
        var packageElement = packageEl(element);
        var prefix = packageElement.toString();
        return name.substring(prefix.length() + 1)
                   .replace('.', '_') + "_Writer";
    }

    private static final String QUO = "\"";

    private static String quote(Object string) {
        return QUO + string + QUO;
    }

    private static Optional<Class<?>> primitiveEvent(RecordComponentElement element) {
        return Arrays.stream(BaseType.values())
            .filter(baseType ->
                baseType.fieldTypes()
                    .stream().anyMatch(fieldType ->
                        fieldType.getName().equals(element.asType().toString())))
            .flatMap(baseType ->
                baseType.fieldTypes()
                    .stream())
            .findFirst();
    }

    private static Optional<RecordComponentElement> enumType(
        RecordComponentElement element,
        Collection<? extends Element> enums
    ) {
        return Optional.of(element)
            .filter(_ ->
                isType(element, enums));
    }

    private static Optional<TypeElement> generatedEvent(
        RecordComponentElement element,
        Collection<? extends Element> roots
    ) {
        var string = element.asType().toString();
        return roots.stream()
            .filter(root ->
                root instanceof TypeElement te && te.getQualifiedName().toString().equals(string))
            .map(TypeElement.class::cast)
            .findFirst();
    }

    private static RecordAttribute resolveDeclared(
        RecordComponentElement element,
        Collection<? extends Element> roots,
        Collection<? extends Element> enums
    ) {
        return enumType(element, enums)
            .map(_ ->
                new RecordAttribute(
                    "Enum",
                    element,
                    Variant.ENUM,
                    null,
                    roots,
                    enums
                ))
            .or(() -> enumListType(element, enums)
                .map(enumListType ->
                    new RecordAttribute(
                        "Enum",
                        element,
                        Variant.ENUM_LIST,
                        (TypeElement) enumListType,
                        roots,
                        enums
                    )))
            .or(() -> primitiveEvent(element)
                .map(event ->
                    new RecordAttribute(
                        event.getSimpleName(),
                        element,
                        Variant.PRIMITIVE,
                        null,
                        roots,
                        enums
                    )))
            .or(() -> generatedEvent(element, roots)
                .map(generatedType ->
                    new RecordAttribute(
                        "Object",
                        element,
                        Variant.GENERATED,
                        generatedType,
                        roots,
                        enums
                    )
                ))
            .or(() -> primitiveListEvent(element)
                .map(primtiveType ->
                    new RecordAttribute(
                        primtiveType.getSimpleName(),
                        element,
                        Variant.PRIMITIVE_LIST,
                        null,
                        roots,
                        enums
                    )
                ))
            .or(() -> generatedListType(element, roots)
                .map(generatedType ->
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
}
