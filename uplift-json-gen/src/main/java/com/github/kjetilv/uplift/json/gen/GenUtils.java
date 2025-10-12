package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;
import module uplift.json;
import module uplift.json.anno;
import com.github.kjetilv.uplift.json.anno.Field;

final class GenUtils {

    static String fieldName(RecordComponentElement el) {
        var field = el.getAnnotation(Field.class);
        return field == null ? el.getSimpleName().toString()
            : field.value();
    }

    static Optional<String> listType(
        RecordComponentElement element,
        Collection<? extends Element> rootElements,
        Collection<? extends Element> enums
    ) {
        var primitiveListType = primitiveListType(element);
        if (primitiveListType.isPresent()) {
            return primitiveListType
                .map(Class::getName);
        }
        var enumListType = enumListType(element, enums);
        if (enumListType.isPresent()) {
            return enumListType
                .map(el -> el.asType().toString());
        }
        var generatedListType = rootElements.stream()
            .filter(rootElement ->
                element.asType().toString().equals(listType(rootElement)))
            .findFirst();
        if (generatedListType.isPresent()) {
            return generatedListType
                .map(el -> el.asType().toString());
        }
        return Optional.empty();
    }

    static Stream<? extends Element> enums(Set<? extends Element> rootElements) {
        return Stream.of(
                rootElements.stream()
                    .filter(element -> element.getKind() == ElementKind.ENUM),
                rootElements.stream().flatMap(el -> {
                    var enclosedElements = el.getEnclosedElements();
                    return enums(new HashSet<>(enclosedElements));
                })
            )
            .flatMap(Function.identity());
    }

    static boolean isType(RecordComponentElement element, Collection<? extends Element> candidates) {
        return isType(element.asType(), candidates);
    }

    static boolean isListType(RecordComponentElement element, Collection<? extends Element> candidates) {
        var elementType = element.asType();
        return isListType(elementType, candidates);
    }

    static String variableName(TypeElement typeElement) {
        var name = typeElement.getSimpleName().toString();
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    static PackageElement packageEl(Element te) {
        var enclosingElement = te.getEnclosingElement();
        return enclosingElement instanceof PackageElement pe
            ? pe
            : packageEl(enclosingElement);
    }

    static Optional<? extends Element> enumListType(
        RecordComponentElement element,
        Collection<? extends Element> enums
    ) {
        return enums.stream()
            .filter(type ->
                listType(type).equals(element.asType().toString()))
            .findFirst();
    }

    static Optional<TypeElement> generatedListType(
        RecordComponentElement element,
        Collection<? extends Element> rootElements
    ) {
        return rootElements.stream()
            .filter(el ->
                el instanceof TypeElement te && listType(te).equals(element.asType().toString()))
            .findFirst()
            .map(TypeElement.class::cast);
    }

    static Optional<Class<?>> primitiveListType(RecordComponentElement element) {
        return Arrays.stream(BaseType.values())
            .filter(el ->
                el.fieldTypes()
                    .stream().anyMatch(fieldType ->
                        listType(fieldType).equals(element.asType().toString())))
            .flatMap(baseType ->
                baseType.fieldTypes()
                    .stream())
            .findFirst();
    }

    static String fqName(TypeElement te) {
        return canonicalClassName(te, true);
    }

    static String builderClassPlain(TypeElement te) {
        return simpleName(te) + "_Builder";
    }

    static String setter(RecordComponentElement el) {
        return "set" + upcased(el.getSimpleName().toString());
    }

    static String adder(RecordComponentElement el) {
        return "add" + upcased(singularVariableName(el));
    }

    static String callbacksClassPlain(TypeElement te) {
        return simpleName(te) + "_Callbacks";
    }

    static String factoryClassQ(PackageElement pe, TypeElement te) {
        return pe.getQualifiedName() + "." + factoryClass(te);
    }

    static String simpleName(TypeElement te) {
        return canonicalClassName(te, false);
    }

    static String importType(Class<?> clazz) {
        return "import " + clazz.getName() + ";";
    }

    static String unq(PackageElement packageElement, Name name) {
        var prefix = packageElement.getQualifiedName().toString();
        var fullName = name.toString();
        return fullName.startsWith(prefix) ? fullName.substring(prefix.length() + 1) : fullName;
    }

    static String factoryClass(TypeElement te) {
        var annotation = te.getAnnotation(JsonRecord.class);
        var name = te.getSimpleName().toString();
        return annotation == null || annotation.factoryClass().isBlank()
            ? name + DEFAULT_SUFFIX
            : annotation.factoryClass();
    }

    static String singularVariableName(RecordComponentElement el) {
        var annotation = el.getAnnotation(Singular.class);
        var plural = fieldName(el);
        return annotation != null ? annotation.value()
            : plural.endsWith("s") ? plural.substring(0, plural.length() - 1)
                : plural;
    }

    private GenUtils() {
    }

    private static final String DEFAULT_SUFFIX = "RW";

    private static boolean isListType(TypeMirror elementType, Collection<? extends Element> candidates) {
        return candidates.stream()
            .map(Element::asType)
            .anyMatch(type ->
                (List.class.getName() + "<" + type + ">").equals(elementType.toString()));
    }

    private static boolean isType(TypeMirror elementType, Collection<? extends Element> candidates) {
        return candidates.stream()
            .map(Element::asType)
            .anyMatch(elementType::equals);
    }

    private static String listType(TypeElement te) {
        return List.class.getName() + "<" + te.getQualifiedName() + ">";
    }

    private static String listType(Class<?> el) {
        return List.class.getName() + "<" + el.getName() + ">";
    }

    private static String listType(Element type) {
        Object type1 = type.asType();
        return List.class.getName() + "<" + type1 + ">";
    }

    private static String canonicalClassName(TypeElement te, boolean fq) {
        var packageName = packageEl(te).toString();
        return (fq ? packageName + "." : "") + te.getQualifiedName().toString()
            .substring(packageName.length() + 1)
            .replace('.', '_');
    }

    private static String upcased(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }
}
