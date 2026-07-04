package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;
import com.github.kjetilv.uplift.json.anno.Field;
import com.github.kjetilv.uplift.json.anno.JsonRecord;
import com.github.kjetilv.uplift.json.anno.Singular;

import javax.lang.model.type.TypeKind;

@SuppressWarnings("unchecked")
final class GenUtils {

    static String fieldName(RecordComponentElement el) {
        var field = el.getAnnotation(Field.class);
        return field == null ? el.getSimpleName().toString()
            : field.value();
    }

    static String variableName(TypeElement typeElement) {
        var name = typeElement.getSimpleName().toString();
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    static PackageElement packageOf(Element te) {
        if (te == null) {
            return null;
        }
        var enclosingElement = te.getEnclosingElement();
        return enclosingElement instanceof PackageElement packageElement
            ? packageElement
            : packageOf(enclosingElement);
    }

    static String fqName(TypeElement te) {
        return canonicalClassName(te, true);
    }

    static String setter(RecordComponentElement el) {
        return "set" + upcased(el.getSimpleName().toString());
    }

    static String adder(RecordComponentElement el) {
        return "add" + upcased(singularVariableName(el));
    }

    static String callbacksClassPlain(TypeMirror te) {
        var element = ((DeclaredType) te).asElement();
        var packageElement = packageOf(element);
        var className = element.toString().substring(packageElement.toString().length() + 1);
        return className.replace('.', '_') + "_Callbacks";
    }

    static String factoryClassQ(PackageElement pe, TypeElement te) {
        var qualifiedName = pe.getQualifiedName();
        return (qualifiedName.isEmpty() ? "" : qualifiedName + ".") + factoryClass(te);
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

    static String builderClassPlain(TypeElement te) {
        return simpleName(te) + "_Builder";
    }

    static String simpleName(TypeElement te) {
        return canonicalClassName(te, false);
    }

    static String callbacksClassPlain(TypeElement te) {
        return simpleName(te) + "_Callbacks";
    }

    private final Types typeUtils;

    private final Elements elementUtils;

    private final TypeMirror iterableErasure;

    private final TypeMirror mapErasure;

    private final List<TypeMatcher> matchers;

    private final TypeMirror recordType;

    private final TypeMirror enumErasure;

    GenUtils(
        Types typeUtils,
        Elements elementUtils
    ) {
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;

        elementUtils.getModuleElement("java.base");

        TypeMirror mapType = fetch(Map.class);
        this.mapErasure = typeUtils.erasure(mapType);

        TypeMirror iterableType = fetch(Iterable.class);
        this.iterableErasure = typeUtils.erasure(iterableType);

        TypeMirror enumType = fetch(Enum.class);
        this.enumErasure = typeUtils.erasure(enumType);

        this.recordType = fetch(Record.class);

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
            matcher(BaseType.INSTANT, Instant.class),
            matcher(BaseType.URL, URL.class),
            matcher(BaseType.URI, URI.class),
            matcher(BaseType.LOCALDATE, LocalDate.class),
            matcher(BaseType.LOCALDATETIME, LocalDateTime.class),
            matcher(BaseType.OFFSETDATETIME, OffsetDateTime.class),
            matcher(BaseType.DURATION, Duration.class),
            matcher(BaseType.UUID, UUID.class),
            matcher(BaseType.MAP, Map.class)
        );
    }

    public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
        return typeUtils.isAssignable(t1, t2) ||
               typeUtils.isAssignable(typeUtils.erasure(t1), typeUtils.erasure(t2));
    }

    public boolean isSameType(TypeMirror t1, TypeMirror t2) {
        return typeUtils.isSameType(t1, t2) ||
               typeUtils.isSameType(typeUtils.erasure(t1), typeUtils.erasure(t2));
    }

    public TypeElement lookup(Class<?> type) {
        return elementUtils.getAllTypeElements(type.getName())
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(type + " not found"));
    }

    public boolean isMap(TypeMirror type) {
        return typeUtils.isAssignable(typeUtils.erasure(type), this.mapErasure);
    }

    public boolean isIterable(RecordComponentElement element) {
        return iterableType(element).isPresent();
    }

    public boolean isArray(RecordComponentElement element) {
        return element.asType().getKind() == TypeKind.ARRAY;
    }

    String simpleName(TypeMirror te) {
        if (typeUtils.asElement(te) instanceof TypeElement typeElement) {
            return simpleName(typeElement);
        }
        throw new IllegalStateException("Not a type element: " + te);
    }

    boolean isMap(RecordComponentElement element) {
        return isMap(element.asType());
    }

    Optional<TypeMirror> iterableType(RecordComponentElement element) {
        if (element.asType() instanceof DeclaredType declared && isIterableType(declared)) {
            return (Optional<TypeMirror>) declared.getTypeArguments()
                .stream()
                .findFirst();
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
    }

    private boolean isIterableType(TypeMirror elementType) {
        return isAssignable(typeUtils.erasure(elementType), iterableErasure);
    }

    private RecordAttribute attribute(TypeMirror parameterType, RecordComponentElement element, boolean list) {
        var attributes = matchers.stream()
            .map(matcher ->
                matcher.recordAttribute(
                    parameterType,
                    element,
                    parameterType,
                    list
                ))
            .flatMap(Optional::stream)
            .toList();
        if (attributes.size() == 1) {
            return attributes.getFirst();
        }
        if (attributes.isEmpty()) {
            if (typeUtils.isAssignable(element.asType(), recordType)) {
                return new RecordAttribute(
                    null,
                    "Object",
                    element,
                    Variant.GENERATED,
                    null
                );
            }
            if (typeUtils.isAssignable(typeUtils.erasure(element.asType()), enumErasure)) {
                return new RecordAttribute(
                    null,
                    "Enum",
                    element,
                    Variant.ENUM,
                    null
                );
            }
            var iteratedType = iterableType(element).orElseThrow(() ->
                new IllegalStateException("No matcher for " + element)
            );
            if (isIterableType(element.asType())) {
                if (isAssignable(iteratedType, recordType)) {
                    return new RecordAttribute(
                        null,
                        "Object",
                        element,
                        Variant.GENERATED_LIST,
                        iteratedType
                    );
                }
                if (typeUtils.isAssignable(typeUtils.erasure(iteratedType), enumErasure)) {
                    return new RecordAttribute(
                        null,
                        "Enum",
                        element,
                        Variant.ENUM_LIST,
                        iteratedType
                    );
                }
            }
        }
        var attributesList = attributes.isEmpty() ? "<no attributes>"
            : attributes.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        throw new IllegalStateException("No matcher for " + element + ": " + attributesList);
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

    private static final String DEFAULT_SUFFIX = "RW";

    private static String canonicalClassName(TypeElement te, boolean fq) {
        var packageElement = packageOf(te);
        var unnamed = packageElement == null || packageElement.isUnnamed();
        var packagePrefix = !fq || unnamed ? "" : packageElement + ".";
        var classNamePart = te.getQualifiedName().toString()
            .substring(unnamed ? 0 : packageElement.getQualifiedName().length() + 1);
        return packagePrefix + classNamePart.replace('.', '_');
    }

    private static String upcased(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }
}
