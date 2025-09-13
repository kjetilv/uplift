package com.github.kjetilv.uplift.json.gen;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.FieldEvents;
import com.github.kjetilv.uplift.json.ObjectWriter;
import com.github.kjetilv.uplift.json.anno.Field;
import com.github.kjetilv.uplift.json.anno.JsonRecord;
import com.github.kjetilv.uplift.json.anno.Singular;

import javax.annotation.processing.Generated;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

record Gen(PackageElement pe, TypeElement te, String time, Function<String, JavaFileObject> filer) {

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
        return isType(element.asType(), candidates);
    }

    static boolean isType(TypeMirror elementType, Collection<? extends Element> candidates) {
        return candidates.stream()
            .map(Element::asType)
            .anyMatch(elementType::equals);
    }

    static boolean isListType(RecordComponentElement element, Collection<? extends Element> candidates) {
        TypeMirror elementType = element.asType();
        return isListType(elementType, candidates);
    }

    static boolean isListType(TypeMirror elementType, Collection<? extends Element> candidates) {
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

    void writeRW(
        TypeElement te,
        JavaFileObject file
    ) {
        Name name = te.getQualifiedName();
        try (BufferedWriter bw = writer(file)) {
            write(
                bw,
                "package " + pe.getQualifiedName() + ";",
                "",
                importType(Consumer.class),
                importType(Function.class),
                importType(Generated.class),
                "",
                importType(Callbacks.class),
                importType(ObjectWriter.class),
                importType(JsonRW.class),
                "",
                "/// Reading and writing instances of [" + unq(pe, name) + "]",
                "///",
                "/// Generated @ " + time + " by " + System.getProperty("user.name") + " using uplift",
                "@" + GENERATED + "(",
                "    value = \"" + JsonRecordProcessor.class.getName() + "\",",
                "    date = \"" + time + "\",",
                "    comments = \"Reading and writing " + unq(pe, name) + "\"",
                ")",
                "public final class " + factoryClass(te) + " implements " + JSON_RW + "<" + unq(pe, name) + "> {",
                "",
                "    public static " + JSON_RW + "<" + unq(pe, name) + "> INSTANCE = new " + factoryClass(te) + "();",
                "",
                "    @Override",
                "    public " + FUNCTION + "<" + CONSUMER + "<" + unq(pe, name) + ">, " + CALLBACKS + "> callbacks() {",
                "        return " + callbacksClassPlain(te) + "::create;",
                "    }",
                "",
                "    @Override",
                "    public " + CALLBACKS + " callbacks(" + CONSUMER + "<" + unq(pe, name) + "> onDone) {",
                "        return " + callbacksClassPlain(te) + ".create(onDone);",
                "    }",
                "",
                "    @Override",
                "    public " + OBJECT_WRITER + "<" + unq(pe, name) + "> objectWriter() {",
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

    void writeWriter(
        JavaFileObject file,
        Collection<? extends Element> roots,
        Collection<? extends Element> enums
    ) {
        try (BufferedWriter bw = writer(file)) {
            Name name = te.getQualifiedName();
            write(
                bw,
                "package " + pe.getQualifiedName() + ";",
                "",
                importType(Generated.class),
                "",
                importType(AbstractObjectWriter.class),
                importType(FieldEvents.class),
                "",
                "/// Writer for [" + unq(pe, name) + "]",
                "///",
                "/// Generated at " + time + " by " + System.getProperty("user.name") + " using uplift",
                "@" + GENERATED + "(",
                "    value = \"" + JsonRecordProcessor.class.getName() + "\",",
                "    date = \"" + time + "\",",
                "    comments = \"Writer for " + unq(pe, name) + "\"",
                ")",
                "final class " + writerClassPlain(te) + " extends " + ABSTRACT_OBJECT_WRITER + "<" + unq(
                    pe,
                    name
                ) + "> {",
                "",
                "    protected " + FIELD_EVENTS + " doWrite(",
                "        " + unq(pe, name) + " " + variableName(te) + ", ",
                "        " + FIELD_EVENTS + " events",
                "    ) {",
                "        return events"
            );
            te.getRecordComponents()
                .stream()
                .map(element -> RecordAttribute.create(element, roots, enums))
                .forEach(recordAttribute ->
                    write(
                        bw,
                        "            ." + recordAttribute.writeCall(te)
                    ));
            write(
                bw,
                "        ;",
                "    }",
                "}"
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write callbacks for " + te, e);
        }

    }

    void writeCallbacks(
        JavaFileObject file,
        Collection<? extends Element> roots,
        Collection<? extends Element> enums,
        boolean isRoot
    ) {
        Name name = te.getQualifiedName();
        try (BufferedWriter bw = writer(file)) {
            write(
                bw,
                "package " + pe.getQualifiedName() + ";",
                "",
                importType(Consumer.class),
                importType(Generated.class),
                "",
                importType(Callbacks.class),
                importType(JsonRecordProcessor.class),
                importType(PresetCallbacks.class),
                importType(PresetCallbacksInitializer.class),
                "",
                "/// Callbacks for [" + unq(pe, name) + "]",
                "///",
                "/// Generated at " + time + " by " + System.getProperty("user.name") + " using uplift",
                "@" + GENERATED + "(",
                "    value = \"" + JsonRecordProcessor.class.getName() + "\",",
                "    date = \"" + time + "\",",
                "    comments = \"Callbacks for " + unq(pe, name) + "\"",
                ")",
                "final class " + callbacksClassPlain(te) + " {",
                "",
                "    static " + PRESET_CALLBACKS + "<" + builderClassPlain(te) + ", " + unq(
                    pe,
                    name
                ) + "> create(",
                "        " + CONSUMER + "<" + unq(pe, name) + "> onDone",
                "    ) {",
                "        return create(null, onDone);",
                "    }",
                "",
                "    static " + PRESET_CALLBACKS + "<" + builderClassPlain(te) + ", " + unq(
                    pe,
                    name
                ) + "> create(",
                "        " + CALLBACKS + " parent, ",
                "        " + CONSUMER + "<" + unq(pe, name) + "> onDone",
                "    ) {",
                "        return new " + PRESET_CALLBACKS + "<>(",
                "            " + builderClassPlain(te) + ".create(),",
                "            parent,",
                "            PRESETS.getNumbers(),",
                "            PRESETS.getStrings(),",
                "            PRESETS.getBooleans(),",
                "            PRESETS.getObjects(),",
                "            onDone,",
                "            PRESETS.getTokenTrie()",
                "        );",
                "    }",
                "",
                "    static final " + PRESET_CALLBACKS_INITIALIZER + "<" + builderClassPlain(te) + ", " + unq(
                    pe,
                    name
                ) + "> PRESETS;"
            );
            write(bw, "");
            write(bw, "    static {");
            write(bw, "        PRESETS = new " + PRESET_CALLBACKS_INITIALIZER + "<>();");
            write(
                bw,
                te.getRecordComponents()
                    .stream()
                    .filter(element ->
                        element.getKind() == ElementKind.RECORD_COMPONENT)
                    .map(element ->
                        "        PRESETS." + RecordAttribute.create(element, roots, enums)
                            .callbackHandler(te) + ";"
                    )
                    .toList()
            );

            List<RecordAttribute> recordAttributes = te.getRecordComponents()
                .stream()
                .filter(recordComponentElement ->
                    recordComponentElement.getKind() == ElementKind.RECORD_COMPONENT)
                .map(element ->
                    RecordAttribute.create(element, roots, enums))
                .toList();

            write(
                bw,
                recordAttributes.stream()
                    .filter(RecordAttribute::isGenerated)
                    .map(attribute ->
                        "        PRESETS.sub(" + callbacksClassPlain(attribute.internalType()) + ".PRESETS);")
                    .toList()
            );

            if (isRoot) {
                write(bw, "        PRESETS.buildTokens(null);");
            }
            write(bw, "    }", "}");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write callbacks for " + te, e);
        }
    }

    void writeBuilder(
        JavaFileObject file,
        Collection<? extends Element> roots,
        Collection<? extends Element> enums
    ) {
        Name name = te.getQualifiedName();

        List<String> setters = te.getRecordComponents()
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

        List<String> adders = te.getRecordComponents()
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
            "    public " + unq(pe, name) + " get() {",
            "        return new " + unq(pe, name) + "("
        );

        List<String> creatorMeat = te.getRecordComponents()
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

        try (BufferedWriter bw = writer(file)) {
            write(
                bw,
                "package " + pe.getQualifiedName() + ";",
                "",
                importType(Supplier.class),
                importType(Generated.class),
                "",
                "/// Builder for [" + unq(pe, name) + "]",
                "///",
                "/// Generated at " + time + " by " + System.getProperty("user.name") + " using uplift",
                "@" + GENERATED + "(",
                "    value = \"" + JsonRecordProcessor.class.getName() + "\",",
                "    date = \"" + time + "\",",
                "    comments = \"Builder for " + unq(pe, name) + "\"",
                ")",
                "final class " + builderClassPlain(te) + " implements " + SUPPLIER + "<" + unq(
                    pe,
                    name
                ) + "> {",
                "",
                "    static " + builderClassPlain(te) + " create() {",
                "        return new " + builderClassPlain(te) + "();",
                "    }",
                "",
                "    private " + builderClassPlain(te) + "() {",
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
            throw new IllegalStateException("Failed to write builder for " + te, e);
        }
    }

    private static final String GENERATED = Generated.class.getSimpleName();

    private static final String PRESET_CALLBACKS = PresetCallbacks.class.getSimpleName();

    private static final String PRESET_CALLBACKS_INITIALIZER = PresetCallbacksInitializer.class.getSimpleName();

    private static final String ABSTRACT_OBJECT_WRITER = AbstractObjectWriter.class.getSimpleName();

    private static final String FIELD_EVENTS = FieldEvents.class.getSimpleName();

    private static final String SUPPLIER = Supplier.class.getSimpleName();

    private static final String JSON_RW = JsonRW.class.getSimpleName();

    private static final String CONSUMER = Consumer.class.getSimpleName();

    private static final String CALLBACKS = Callbacks.class.getSimpleName();

    private static final String FUNCTION = Function.class.getSimpleName();

    private static final String OBJECT_WRITER = ObjectWriter.class.getSimpleName();

    private static final String DEFAULT_SUFFIX = "RW";

    private static String importType(Class<?> clazz) {
        return "import " + clazz.getName() + ";";
    }

    private static String unq(PackageElement packageElement, Name name) {
        String prefix = packageElement.getQualifiedName().toString();
        String fullName = name.toString();
        return fullName.startsWith(prefix) ? fullName.substring(prefix.length() + 1) : fullName;
    }

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
