package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;
import com.github.kjetilv.uplift.json.anno.Field;
import com.github.kjetilv.uplift.json.anno.JsonRecord;
import com.github.kjetilv.uplift.json.anno.Singular;

import javax.lang.model.type.TypeKind;

@SuppressWarnings("unchecked")
final class GenUtils {

    static String baseJsonType(RecordComponentElement el) {
        return BaseType.of(el).typeName();
    }

    static String fieldName(RecordComponentElement el) {
        var field = el.getAnnotation(Field.class);
        return field == null ? el.getSimpleName().toString()
            : field.value();
    }

    static Stream<? extends DeclaredType> enums(Collection<? extends Element> els) {
        return Stream.concat(
            els.stream()
                .filter(element ->
                    element.getKind() == ElementKind.ENUM)
                .map(Element::asType)
                .map(DeclaredType.class::cast),
            els.stream()
                .map(Element::getEnclosedElements)
                .flatMap(GenUtils::enums)
        );
    }

    static String variableName(TypeElement typeElement) {
        var name = typeElement.getSimpleName().toString();
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    static PackageElement packageOf(Element te) {
        var enclosingElement = te.getEnclosingElement();
        return enclosingElement instanceof PackageElement packageElement
            ? packageElement
            : packageOf(enclosingElement);
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

//    static Optional<? extends Element> enumListType(
//        RecordComponentElement element,
//        Collection<? extends DeclaredType> enums
//    ) {
//        return enums.stream()
//            .filter(type ->
//                listType(type).equals(element.asType().toString()))
//            .findFirst();
//    }

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
        var qualifiedName = pe.getQualifiedName();
        return (qualifiedName.isEmpty() ? "" : qualifiedName + ".") + factoryClass(te);
    }

    static String simpleName(TypeElement te) {
        return canonicalClassName(te, false);
    }

    static String importType(Class<?> clazz) {
        return "import " + clazz.getName() + ";";
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

    private final Types typeUtils;

    private final Elements elementUtils;

    private final ModuleElement javaBase;

    private final PackageElement javaUtil;

    private final TypeMirror mapType;

    private final TypeMirror iterableType;

    private final TypeMirror iterableErasure;

    private final TypeMirror mapErasure;

    private final List<TypeMatcher> matchers;

    GenUtils(
        Types typeUtils,
        Elements elementUtils
    ) {
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;

        this.javaBase = elementUtils.getModuleElement("java.base");
        this.javaUtil = elementUtils.getPackageElement("java.util");

        this.mapType = fetch(Map.class);
        this.mapErasure = typeUtils.erasure(this.mapType);

        this.iterableType = fetch(Iterable.class);
        this.iterableErasure = typeUtils.erasure(this.iterableType);

        this.matchers = List.of(
            matcher(BaseType.STRING, String.class),
            matcher(BaseType.BOOLEAN, Boolean.class, Boolean.TYPE),
            matcher(BaseType.BYTE, Byte.class, Byte.TYPE),
            matcher(BaseType.SHORT, Short.class, Short.TYPE),
            matcher(BaseType.INTEGER, Integer.class, Integer.TYPE),
            matcher(BaseType.LONG, Long.class, Long.TYPE),
            matcher(BaseType.CHAR, Character.class, Character.TYPE),
            matcher(BaseType.FLOAT, Float.class, Float.TYPE),
            matcher(BaseType.DOUBLE, Double.class, Double.TYPE),
            matcher(BaseType.BIG_DECIMAL, BigDecimal.class),
            matcher(BaseType.BIG_INTEGER, BigInteger.class),
            matcher(BaseType.UUID, UUID.class),
            matcher(BaseType.MAP, Map.class)
        );
    }

    public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
        return typeUtils.isAssignable(t1, t2)  ||
               typeUtils.isAssignable(typeUtils.erasure(t1), typeUtils.erasure(t2));
    }

    public boolean isSameType(TypeMirror t1, TypeMirror t2) {
        return typeUtils.isSameType(t1, t2) ||
               typeUtils.isSameType(typeUtils.erasure(t1), typeUtils.erasure(t2));
    }

    public Element asElement(TypeMirror type) {
        return typeUtils.asElement(type);
    }

    boolean isMap(RecordComponentElement element) {
        return typeUtils.isAssignable(typeUtils.erasure(element.asType()), this.mapErasure);
    }

//    Optional<TypeMirror> fieldType(
//        RecordComponentElement element,
//        Collection<? extends DeclaredType> candidates
//    ) {
//        var type = element.asType();
//        return (Optional<TypeMirror>) candidates.stream()
//            .filter(candidate ->
//                typeUtils.isSameType(type, candidate))
//            .findFirst();
//    }

    Optional<PrimitiveType> primitiveType(
        RecordComponentElement element
    ) {
        return Optional.ofNullable(element.asType().getKind())
            .filter(TypeKind::isPrimitive)
            .map(typeUtils::getPrimitiveType);
    }

//    Optional<TypeMirror> listType(
//        RecordComponentElement element,
//        Collection<? extends Element> jsonRecords,
//        Collection<? extends Element> enums
//    ) {
//        var primitiveListType = primitiveListType(element);
//        if (primitiveListType.isPresent()) {
//            return primitiveListType
//                .map(Class::getName);
//        }
//        var enumListType = enumListType(element, enums);
//        if (enumListType.isPresent()) {
//            return enumListType
//                .map(Element::asType)
//                .map(TypeMirror::toString);
//        }
//        var generatedListType = jsonRecords.stream()
//            .filter(rootElement ->
//                element.asType().toString().equals(listType(rootElement)))
//            .findFirst();
//        if (generatedListType.isPresent()) {
//            return generatedListType
//                .map(Element::asType);
//        }
//        return Optional.empty();
//    }

    Optional<TypeMirror> iterableType(RecordComponentElement element) {
        var elementType = element.asType();
        if (elementType instanceof DeclaredType declared) {
            if (isAssignable(typeUtils.erasure(elementType), iterableErasure)) {
                return (Optional<TypeMirror>) declared.getTypeArguments()
                    .stream()
                    .findFirst();
            }
        }
        return Optional.empty();
    }

    <T extends TypeMirror> T fetchPrimitive(Class<?> type) {
        if (type == Boolean.TYPE) {
            return (T) typeUtils.getPrimitiveType(TypeKind.BOOLEAN);
        }
        if (type == Integer.TYPE) {
            return (T) typeUtils.getPrimitiveType(TypeKind.INT);
        }

        if (type == Long.TYPE) {
            return (T) typeUtils.getPrimitiveType(TypeKind.LONG);
        }

        if (type == Short.TYPE) {
            return (T) typeUtils.getPrimitiveType(TypeKind.SHORT);
        }

        if (type == Byte.TYPE) {
            return (T) typeUtils.getPrimitiveType(TypeKind.BYTE);
        }

        if (type == Float.TYPE) {
            return (T) typeUtils.getPrimitiveType(TypeKind.FLOAT);
        }

        if (type == Double.TYPE) {
            return (T) typeUtils.getPrimitiveType(TypeKind.DOUBLE);
        }
        if (type == Character.TYPE) {
            return (T) typeUtils.getPrimitiveType(TypeKind.CHAR);
        }
        throw new IllegalStateException(type.getName());
    }

    <T extends TypeMirror> T fetch(Class<?> type) {
        return (T) elementUtils.getAllTypeElements(type.getName())
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(type + " not found"))
            .asType();
    }

    RecordAttribute create(RecordComponentElement element) {
        return iterableType(element)
            .map(iterableType ->
                attribute(iterableType, element, true))
            .orElseGet(() ->
                attribute(element.asType(), element, false));
//        return switch (type.getKind()) {
//            case BOOLEAN -> primitive("Boolean", element, roots, enums);
//            case BYTE -> primitive("Byte", element, roots, enums);
//            case SHORT -> primitive("Short", element, roots, enums);
//            case INT -> primitive("Integer", element, roots, enums);
//            case LONG -> primitive("Long", element, roots, enums);
//            case CHAR -> primitive("Character", element, roots, enums);
//            case FLOAT -> primitive("Float", element, roots, enums);
//            case DOUBLE -> primitive("Double", element, roots, enums);
//            case DECLARED -> resolveDeclared(element, roots, enums);
//            default -> throw new IllegalStateException("Unsupported: " + element);
//        };
    }

    private RecordAttribute attribute(TypeMirror parameterType, RecordComponentElement element, boolean list) {
        var attributes = matchers.stream()
            .map(matcher ->
                matcher.recordAttribute(parameterType, element, list))
            .flatMap(Optional::stream)
            .toList();
        if (attributes.size() == 1) {
            return attributes.getFirst();
        }
        throw new IllegalStateException("Invalid matchers for " + element + ": " + attributes.stream()
            .map(Object::toString)
            .collect(Collectors.joining(", ")));
    }

    private TypeMatcher matcher(BaseType baseType, Class<?> type) {
        return matcher(baseType, type, null);
    }

    private TypeMatcher matcher(BaseType baseType, Class<?> type, Class<?> primitiveType) {
        var primitiveTypeMirror = primitiveType == null
            ? null
            : fetchPrimitive(primitiveType);
        return new TypeMatcher(
            baseType,
            type,
            primitiveType,
            fetch(type),
            primitiveTypeMirror,
            this
        );
    }

//    private DeclaredType resolveDeclared(
//        RecordComponentElement element,
//        Collection<? extends DeclaredType> roots,
//        Collection<? extends DeclaredType> enums
//    ) {
//        var baseType = matchers.stream()
//            .filter(matcher -> matcher.matches(element))
//            .findFirst()
//            .map(matcher ->
//                new RecordAttribute(
//                    matcher.baseType().
//                ));
//        return baseType.or(() ->
//            fieldType(element, enums)
//                .map(enumType ->
//                    new RecordAttribute(
//                        "Enum",
//                        element,
//                        Variant.ENUM,
//                        null,
//                        roots,
//                        enums
//                    ))
//                .or(() ->
//                    iterableType(element, enums)
//                        .map(enumListType ->
//                            new RecordAttribute(
//                                "Enum",
//                                element,
//                                Variant.ENUM_LIST,
//                                (TypeElement) enumListType.asElement(),
//                                roots,
//                                enums
//                            )))
//                .or(() ->
//                    fieldType(element, roots)
//                        .map(generatedType ->
//                            new RecordAttribute(
//                                "Object",
//                                element,
//                                Variant.GENERATED,
//                                (TypeElement) generatedType.asElement(),
//                                roots,
//                                enums
//                            )
//                        ))
//                .or(() -> iterableType(element, roots)
//                    .map(enumListType ->
//                        new RecordAttribute(
//                            "Object",
//                            element,
//                            Variant.GENERATED_LIST,
//                            (TypeElement) enumListType.asElement(),
//                            roots,
//                            enums
//                        )
//                    ))
//                .or(() -> primitiveType(element)
//                    .map(primitiveType ->
//                        new RecordAttribute(
//                            primitiveType.getKind().toString(),
//                            element,
//                            Variant.PRIMITIVE,
//                            null,
//                            roots,
//                            enums
//                        )))
//                .or(() -> primitiveListType(element)
//                    .map(primtiveType ->
//                        new RecordAttribute(
//                            primtiveType.getSimpleName(),
//                            element,
//                            Variant.PRIMITIVE_LIST,
//                            null,
//                            roots,
//                            enums
//                        )
//                    ))

    /// /            .or(() -> generatedListType(element, roots)
    /// /                .map(generatedType ->
    /// /                    new RecordAttribute(
    /// /                        "Object",
    /// /                        element,
    /// /                        Variant.GENERATED_LIST,
    /// /                        generatedType,
    /// /                        roots,
    /// /                        enums
    /// /                    )
    /// /                ))
    /// /            .or(() -> Optional.of(element.asType().toString())
    /// /                .filter(mapType ->
    /// /                    mapType.startsWith("java.util.Map"))
    /// /                .map(_ ->
    /// /                    new RecordAttribute(
    /// /                        "Object",
    /// /                        element,
    /// /                        Variant.GENERIC_MAP,
    /// /                        null,
    /// /                        roots,
    /// /                        enums
    /// /                    ))
    /// /            )
//                .orElseThrow(() ->
//                    new IllegalStateException("Unsupported element/type: " + element + "/" + element.asType() + ", roots are: " + roots));
//    }
    private Predicate<TypeMirror> fieldType(TypeMirror typeArgument) {
        return type -> typeUtils.isSameType(typeArgument, type);
    }

    private Optional<DeclaredType> iterableType(DeclaredType declared) {
        return typeUtils.directSupertypes(declared)
            .stream()
            .filter(DeclaredType.class::isInstance)
            .filter(superType ->
                typeUtils.isAssignable(iterableErasure, superType))
            .findFirst()
            .map(DeclaredType.class::cast);
    }

    private static final String DEFAULT_SUFFIX = "RW";

    private static String listType(TypeElement te) {
        return List.class.getName() + "<" + te.getQualifiedName() + ">";
    }

    private static String listType(Class<?> el) {
        return List.class.getName() + "<" + el.getName() + ">";
    }

    private static String canonicalClassName(TypeElement te, boolean fq) {
        var packageElement = packageOf(te);
        return (fq && !packageElement.isUnnamed() ? packageElement + "." : "") + te.getQualifiedName().toString()
            .substring(packageElement.isUnnamed() ? 0 : packageElement.getQualifiedName().length() + 1)
            .replace('.', '_');
    }

    private static String upcased(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

}
