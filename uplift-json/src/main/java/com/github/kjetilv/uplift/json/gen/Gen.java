package com.github.kjetilv.uplift.json.gen;

import com.github.kjetilv.uplift.json.*;

import javax.annotation.processing.Generated;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Gen {

    static void writeRW(PackageElement pe, TypeElement te, JavaFileObject file) {
        Name name = te.getQualifiedName();
        try (BufferedWriter bw = writer(file)) {
            write(
                bw,
                "package " + pe.getQualifiedName() + ";",
                "",
                "@" + Generated.class.getName() + "(",
                "    value = \"" + JsonRecordProcessor.class.getName() + "\",",
                "    date = \"" + time() + "\"",
                ")",
                "public final class " + factoryClass(te),
                "    implements " + JsonRW.class.getName() + "<",
                "        " + name,
                "    > {",
                "",
                "    public static com.github.kjetilv.uplift.json.gen.JsonRW<",
                "        " + name,
                "    > INSTANCE = new " + factoryClass(te) + "();",
                "",
                "    @Override",
                "    public " + Function.class.getName() + "< ",
                "        " + Consumer.class.getName() + "<" + name + ">,",
                "        " + Callbacks.class.getName(),
                "    > callbacks() {",
                "        return " + callbacksClassPlain(te) + "::create;",
                "    }",
                "",
                "    @Override",
                "    public " + packageEl(te) + "." + callbacksClassPlain(te) + " callbacks(",
                "        " + Consumer.class.getName() + "<" + name + "> onDone",
                "    ) {",
                "        return " + packageEl(te) + "." + callbacksClassPlain(te) + ".create(onDone);",
                "    }",
                "",
                "    @Override",
                "    public " + ObjectWriter.class.getName() + "<" + name + "> objectWriter() {",
                "        return new " + writerClassPlain(te) + "();",
                "    }",
                "",
                "    private " + factoryClass(te) + "() {",
                "    }",
                "}"
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write factory for " + te, e);
        }

    }

    static void writeWriter(
        PackageElement packageElement,
        TypeElement typeElement,
        JavaFileObject file,
        Collection<? extends Element> roots,
        Collection<? extends Element> enums
    ) {
        try (BufferedWriter bw = writer(file)) {
            write(
                bw,
                "package " + packageElement.getQualifiedName() + ";",
                "",
                "@" + Generated.class.getName() + "(",
                "    value = \"" + JsonRecordProcessor.class.getName() + "\",",
                "    date = \"" + time() + "\"",
                ")",
                "final class " + writerClassPlain(typeElement),
                "    extends " + AbstractObjectWriter.class.getName() + "<",
                "        " + typeElement.getQualifiedName(),
                "    > {",
                "",
                "    protected " + FieldEvents.class.getName() + " doWrite(",
                "        " + typeElement.getQualifiedName() + " " + variableName(typeElement) + ", ",
                "        " + FieldEvents.class.getName() + " events",
                "    ) {",
                "        return events"
            );
            typeElement.getRecordComponents()
                .stream()
                .map(element -> RecordAttribute.create(element, roots, enums))
                .forEach(recordAttribute ->
                    write(
                        bw,
                        "            ." + recordAttribute.writeCall(typeElement)
                    ));
            write(
                bw,
                "        ;",
                "    }",
                "}"
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write callbacks for " + typeElement, e);
        }

    }

    static void writeBuilder(
        PackageElement packageElement,
        TypeElement typeElement,
        JavaFileObject file,
        Collection<? extends Element> roots,
        Collection<? extends Element> enums
    ) {
        List<String> setters = typeElement.getRecordComponents()
            .stream().flatMap(el ->
                Stream.of(
                    "    private " + el.asType() + " " + fieldName(el) + ";",
                    "",
                    "    void " + setter(el) + "(" + el.asType() + " " + fieldName(el) + ") {",
                    "        this." + fieldName(el) + " = " + fieldName(el) + ";",
                    "    }",
                    ""
                ))
            .toList();

        List<String> adders = typeElement.getRecordComponents()
            .stream()
            .flatMap(element ->
                listType(element, roots, enums)
                    .stream()
                    .flatMap(listType ->
                        Stream.of(
                            "    void " + adder(element) + "(" + listType + " " + singularVariableName(element) + ") {",
                            "        if (this." + fieldName(element) + " == null) {",
                            "            this." + fieldName(element) + " = new " + ArrayList.class.getName() + "();",
                            "        }",
                            "        this." + fieldName(element) + ".add(" + singularVariableName(element) + ");",
                            "    }",
                            ""
                        ))
            )
            .toList();

        List<String> creatorStart = List.of(
            "    @Override",
            "    public " + typeElement.asType() + " get() {",
            "        return new " + typeElement.asType() + "("
        );

        List<String> creatorMeat = typeElement.getRecordComponents()
            .stream()
            .map(el ->
                "            " + fieldName(el) + ",")
            .collect(Collectors.toCollection(LinkedList::new));
        String last = creatorMeat.removeLast();
        creatorMeat.addLast(last.substring(0, last.length() - 1));

        List<String> creatorEnd = List.of(
            "        );",
            "    }"
        );

        Name name = typeElement.getQualifiedName();
        try (BufferedWriter bw = writer(file)) {
            write(
                bw,
                "package " + packageElement.getQualifiedName() + ";",
                "",
                "@" + Generated.class.getName() + "(",
                "    value = \"" + JsonRecordProcessor.class.getName() + "\",",
                "    date = \"" + time() + "\"",
                ")",
                "final class " + builderClassPlain(typeElement),
                "    implements " + Supplier.class.getName() + "<",
                "        " + name,
                "    > {",
                "",
                "    static " + builderClassPlain(typeElement) + " create() {",
                "        return new " + builderClassPlain(typeElement) + "();",
                "    }",
                "",
                "    private " + builderClassPlain(typeElement) + "() {",
                "    }"
            );
            write(bw, "");
            write(bw, setters);
            write(bw, adders);
            write(bw, creatorStart);
            write(bw, creatorMeat);
            write(bw, creatorEnd);
            write(bw, "}");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write builder for " + typeElement, e);
        }
    }

    static void writeCallbacks(
        PackageElement packageElement,
        TypeElement typeElement,
        JavaFileObject file,
        Collection<? extends Element> roots,
        Collection<? extends Element> enums
    ) {
        Name name = typeElement.getQualifiedName();
        try (BufferedWriter bw = writer(file)) {
            write(
                bw,
                "package " + packageElement.getQualifiedName() + ";",
                "",
                "@" + Generated.class.getName() + "(",
                "    value = \"" + JsonRecordProcessor.class.getName() + "\",",
                "    date = \"" + time() + "\"",
                ")",
                "final class " + callbacksClassPlain(typeElement),
                "    extends " + AbstractCallbacks.class.getName() + "<",
                "        " + packageEl(typeElement) + "." + builderClassPlain(typeElement) + ",",
                "        " + name,
                "    > {",
                "",
                "    static " + callbacksClassPlain(typeElement) + " create(",
                "        " + Consumer.class.getName() + "<" + name + "> onDone",
                "    ) {",
                "        return create(null, onDone);",
                "    }",
                "",
                "    static " + callbacksClassPlain(typeElement) + " create(",
                "        " + AbstractCallbacks.class.getName() + "<?, ?> parent,",
                "        " + Consumer.class.getName() + "<" + name + "> onDone",
                "    ) {",
                "        return new " + packageEl(typeElement) + "." + callbacksClassPlain(typeElement) + "(parent, onDone);",
                "    }",
                "",
                "    " + callbacksClassPlain(typeElement) + "(",
                "        " + AbstractCallbacks.class.getName() + "<?, ?> parent, ",
                "        " + Consumer.class.getName() + "<" + name + "> onDone",
                "    ) {",
                "        super(" + packageEl(typeElement) + "." + builderClassPlain(typeElement) + ".create(), parent, onDone);"
            );
            write(
                bw, typeElement.getRecordComponents()
                    .stream()
                    .filter(recordComponentElement ->
                        recordComponentElement.getKind() == ElementKind.RECORD_COMPONENT)
                    .map(element ->
                        "        " + RecordAttribute.create(element, roots, enums).callbackHandler(typeElement) + ";"
                    )
                    .toList()
            );
            write(bw, "    }");
            write(bw, "}");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write callbacks for " + typeElement, e);
        }
    }

    static String fieldName(RecordComponentElement el) {
        Field field = el.getAnnotation(Field.class);
        return field == null ? el.getSimpleName().toString()
            : field.value();
    }

    static Optional<String> listType(
        RecordComponentElement element,
        Collection<? extends Element> rootElements,
        Collection<? extends Element> enums
    ) {
        Optional<Class<?>> primitiveListType = primitiveListType(element);
        if (primitiveListType.isPresent()) {
            return primitiveListType
                .map(Class::getName);
        }
        Optional<? extends Element> enumListType = enumListType(element, enums);
        if (enumListType.isPresent()) {
            return enumListType
                .map(el -> el.asType().toString());
        }
        Optional<? extends Element> generatedListType = rootElements.stream()
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
                    List<? extends Element> enclosedElements = el.getEnclosedElements();
                    return enums(new HashSet<>(enclosedElements));
                })
            )
            .flatMap(Function.identity());
    }

    static boolean isType(RecordComponentElement element, Collection<? extends Element> candidates) {
        TypeMirror elementType = element.asType();
        return candidates.stream()
            .map(Element::asType)
            .anyMatch(elementType::equals);
    }

    static boolean isListType(RecordComponentElement element, Collection<? extends Element> candidates) {
        TypeMirror elementType = element.asType();
        return candidates.stream()
            .map(Element::asType)
            .anyMatch(type ->
                listType(type).equals(elementType.toString()));
    }

    static String variableName(TypeElement typeElement) {
        String name = typeElement.getSimpleName().toString();
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    static PackageElement packageEl(Element te) {
        Element enclosingElement = te.getEnclosingElement();
        return enclosingElement instanceof PackageElement pe
            ? pe
            : packageEl(enclosingElement);
    }

    static Optional<RecordComponentElement> enumType(
        RecordComponentElement element,
        Collection<? extends Element> enums
    ) {
        return isType(element, enums)
            ? Optional.of(element)
            : Optional.empty();
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

    static Optional<Class<?>> primitiveEvent(RecordComponentElement element) {
        return Arrays.stream(BaseType.values())
            .filter(baseType ->
                baseType.fieldTypes()
                    .stream().anyMatch(fieldType -> fieldType.getName().equals(element.asType().toString())))
            .flatMap(baseType ->
                baseType.fieldTypes()
                    .stream())
            .findFirst();
    }

    static Optional<TypeElement> generatedEvent(
        RecordComponentElement element,
        Collection<? extends Element> roots
    ) {
        String string = element.asType().toString();
        return roots.stream()
            .filter(root -> root instanceof TypeElement te && te.getQualifiedName().toString().equals(string))
            .map(TypeElement.class::cast)
            .findFirst();
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

    static String fqName(TypeElement te) {
        return canonicalClassName(te, true);
    }

    private Gen() {
    }

    private static final String DEFAULT_SUFFIX = "RW";

    private static String listType(TypeElement te) {
        return listType(te.getQualifiedName());
    }

    private static String listType(Class<?> el) {
        return listType(el.getName());
    }

    private static String listType(Element type) {
        return listType(type.asType());
    }

    private static void write(BufferedWriter bw, String... strs) {
        write(bw, Arrays.asList(strs));
    }

    private static void write(BufferedWriter bw, List<String> strs) {
        strs.forEach(str -> {
            try {
                write(bw, str);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to write line `" + str + "`", e);
            }
        });
    }

    private static void write(BufferedWriter bw, String str) {
        try {
            bw.write(str + "\n");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write line `" + str + "`", e);
        }
    }

    private static BufferedWriter writer(JavaFileObject builder) {
        try {
            return new BufferedWriter(builder.openWriter());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write to " + builder, e);
        }
    }

    private static String writerClassPlain(TypeElement te) {
        return simpleName(te) + "_Writer";
    }

    private static String canonicalClassName(TypeElement te, boolean fq) {
        String packageName = packageEl(te).toString();
        return (fq ? packageName + "." : "") + te.getQualifiedName().toString()
            .substring(packageName.length() + 1)
            .replace('.', '_');
    }

    private static String factoryClass(TypeElement te) {
        JsonRecord annotation = te.getAnnotation(JsonRecord.class);
        String name = te.getSimpleName().toString();
        return annotation == null || annotation.factoryClass().isBlank()
            ? name + DEFAULT_SUFFIX
            : annotation.factoryClass();
    }

    private static String time() {
        return Instant.now().truncatedTo(ChronoUnit.SECONDS)
            .atZone(ZoneId.of("Z"))
            .format(DateTimeFormatter.ISO_DATE_TIME);
    }

    private static String singularVariableName(RecordComponentElement el) {
        Singular annotation = el.getAnnotation(Singular.class);
        String plural = fieldName(el);
        return annotation != null ? annotation.value()
            : plural.endsWith("s") ? plural.substring(0, plural.length() - 1)
                : plural;
    }

    private static String listType(Object type) {
        return List.class.getName() + "<" + type + ">";
    }

    private static String upcased(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }
}
