package com.github.kjetilv.uplift.json.gen;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.FieldEvents;
import com.github.kjetilv.uplift.json.ObjectWriter;

import javax.annotation.processing.Generated;
import javax.lang.model.element.*;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.kjetilv.uplift.json.gen.GenUtils.*;

record Generator(
    PackageElement pe,
    TypeElement te,
    String time,
    Function<String, JavaFileObject> filer
) {

    void writeRW(TypeElement te) {
        Name name = te.getQualifiedName();
        JavaFileObject file = factoryFile(pe, te);
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
        Collection<? extends Element> roots,
        Collection<? extends Element> enums
    ) {
        JavaFileObject file = writerFile(te);
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
        Collection<? extends Element> roots,
        Collection<? extends Element> enums,
        boolean root
    ) {
        Name name = te.getQualifiedName();
        JavaFileObject file = callbackFile(te);
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

            if (root) {
                write(bw, "        PRESETS.buildTokens(null);");
            }
            write(bw, "    }", "}");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write callbacks for " + te, e);
        }
    }

    void writeBuilder(
        Collection<? extends Element> roots,
        Collection<? extends Element> enums
    ) {
        Name name = te.getQualifiedName();
        JavaFileObject file = builderFile(te);
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

    private JavaFileObject factoryFile(PackageElement packageEl, TypeElement typeEl) {
        return file(factoryClassQ(packageEl, typeEl));
    }

    private JavaFileObject callbackFile(TypeElement typeEl) {
        return classFileName(typeEl, "Callbacks");
    }

    private JavaFileObject writerFile(TypeElement typeEl) {
        return classFileName(typeEl, "Writer");
    }

    private JavaFileObject builderFile(TypeElement typeEl) {
        return classFileName(typeEl, "Builder");
    }

    private JavaFileObject classFileName(TypeElement typeEl, String callbacks) {
        return file(fqName(typeEl) + '_' + callbacks);
    }

    private JavaFileObject file(String name) {
        try {
            return filer.apply(name);
        } catch (Exception e) {
            throw new IllegalStateException("Could not open file " + name, e);
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
}
