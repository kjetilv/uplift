package com.github.kjetilv.uplift.json.annpro;

import com.github.kjetilv.uplift.json.AbstractObjectWriter;
import com.github.kjetilv.uplift.json.FieldEvents;
import com.github.kjetilv.uplift.json.anno.JsonRecord;
import com.github.kjetilv.uplift.json.events.AbstractCallbacks;
import com.github.kjetilv.uplift.json.events.AbstractJsonRW;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SupportedAnnotationTypes("com.github.kjetilv.uplift.json.anno.*")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public final class JsonRecordProcessor extends AbstractProcessor {

    static String builderClass(TypeElement te) {
        return te.getSimpleName() + "Builder";
    }

    static String callbacksClass(TypeElement te) {
        return te.getSimpleName() + "Callbacks";
    }

    static String writerClass(TypeElement te) {
        return te.getSimpleName() + "Writer";
    }

    static String writerClass(Object te) {
        return te + "Writer";
    }

    @Override
    public boolean process(Set<? extends TypeElement> typedElements, RoundEnvironment roundEnv) {
        if (isJsonRecord(typedElements)) {
            return Records.mapFields(roundEnv.getRootElements(), processingEnv.getTypeUtils())
                .map(records -> {

                    Set<? extends Element> enums = enums(roundEnv);
                    Set<? extends Element> types = types(roundEnv, enums);

                    if (records.types().stream().noneMatch(JsonRecordProcessor::isRoot)) {
                        throw new IllegalStateException(
                            "None of " + types.size() + " elements are roots: " + write(types));
                    } else {
                        types.forEach(element -> {
                            if (
                                element instanceof TypeElement te && te.getEnclosingElement() instanceof PackageElement pe
                            ) {
                                process(te, pe, types, enums, records.get(te));
                            } else {
                                throw new IllegalStateException("Not a supported type: " + element);
                            }
                        });
                    }
                    return true;
                }).orElse(false);
        }
        return true;
    }

    private void process(
        TypeElement te,
        PackageElement pe,
        Set<? extends Element> roots,
        Set<? extends Element> enums,
        RecordFields records
    ) {
        JsonRecord jsonRecord = te.getAnnotation(JsonRecord.class);
        boolean read = !jsonRecord.writeOnly();
        boolean write = !jsonRecord.readOnly();
        boolean root = isRoot(te);
        if (read) {
            writeBuilder(builderFile(te), records);
            writeCallbacks(pe, te, callbackFile(te), roots, enums);
        }
        if (write) {
            writeWriter(pe, te, writerFile(te), roots, enums, processingEnv.getTypeUtils());
        }
        if (!(read || write)) {
            throw new IllegalArgumentException("Should be readable, writable or both: " + te);
        }
        if (root) {
            writeRW(pe, te, factoryFile(pe, te), read, write);
        }
    }

    private JavaFileObject factoryFile(PackageElement pe, TypeElement te) {
        return file(pe.getQualifiedName() + "." + rwClass(te));
    }

    private JavaFileObject callbackFile(TypeElement typeElement) {
        return file(typeElement, "Callbacks");
    }

    private JavaFileObject writerFile(TypeElement typeElement) {
        return file(typeElement, "Writer");
    }

    private JavaFileObject builderFile(TypeElement typeElement) {
        return file(typeElement, "Builder");
    }

    private JavaFileObject file(TypeElement te, String type) {
        return file(te.getQualifiedName() + type);
    }

    private JavaFileObject file(String name) {
        try {
            return processingEnv.getFiler().createSourceFile(name);
        } catch (Exception e) {
            throw new IllegalStateException("Could not open file " + name, e);
        }
    }

    private void writeBuilder(JavaFileObject file, RecordFields records) {
        List<String> setters = records.values()
            .stream().flatMap(recordField ->
                Stream.of(
                    "    private " + recordField.fieldTypeMirror().toString() + " " + recordField.fieldName() + ";",
                    "",
                    "    void " + recordField.setter() + "(" + recordField.fieldTypeMirror() + " " + recordField.fieldName() + ") {",
                    "        this." + recordField.fieldName() + " = " + recordField.fieldName() + ";",
                    "    }",
                    ""
                ))
            .toList();

        List<String> adders = records.values()
            .stream()
            .filter(RecordField::isCollection)
            .flatMap(recordField ->
                Stream.of(
                    "    void " + recordField.adder() + "(" + recordField.collectionTypeMirror() + " " + recordField.singularName() + ") {",
                    "        if (this." + recordField.fieldName() + " == null) {",
                    "            this." + recordField.fieldName() + " = new " + ArrayList.class.getName() + "();",
                    "        }",
                    "        this." + recordField.fieldName() + ".add(" + recordField.singularName() + ");",
                    "    }",
                    ""
                ))
            .toList();

        TypeElement te = records.typeElement();
        List<String> creatorStart = List.of(
            "    @Override",
            "    public " + te + " get() {",
            "        return new " + te + "("
        );

        List<String> creatorMeat = te.getRecordComponents()
            .stream()
            .map(el ->
                "            " + Gen.fieldName(el) + ",")
            .collect(Collectors.toCollection(LinkedList::new));
        String last = creatorMeat.removeLast();
        creatorMeat.addLast(last.substring(0, last.length() - 1));

        List<String> creatorEnd = List.of(
            "        );",
            "    }"
        );

        try (BufferedWriter bw = Gen.writer(file)) {
            Gen.write(bw, "package " + records.packageElement().getQualifiedName() + ";");
            Gen.write(bw, "");
            Gen.write(
                bw,
                "final class " + builderClass(te) +
                " implements " + Supplier.class.getName() + "<" + te.getSimpleName() + ">{",
                "",
                "    static " + builderClass(te) + " create() {",
                "        return new " + builderClass(te) + "();",
                "    }",
                "",
                "    private " + builderClass(te) + "() {",
                "    }",
                ""
            );
            Gen.write(bw, "");
            Gen.write(bw, setters);
            Gen.write(bw, adders);
            Gen.write(bw, creatorStart);
            Gen.write(bw, creatorMeat);
            Gen.write(bw, creatorEnd);
            Gen.write(bw, "}");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write builder for " + te, e);
        }
    }

    private void writeCallbacks(
        PackageElement pe,
        TypeElement te,
        JavaFileObject file,
        Set<? extends Element> roots,
        Set<? extends Element> enums
    ) {
        Name name = te.getSimpleName();
        try (BufferedWriter bw = Gen.writer(file)) {
            Gen.write(
                bw,
                "package " + pe.getQualifiedName() + ";",
                "",
                "final class " + callbacksClass(te),
                "    extends " + AbstractCallbacks.class.getName() + "<" + builderClass(te) + ", " + name + "> {",
                "",
                "    static " + callbacksClass(te) + " create(",
                "        " + Consumer.class.getName() + "<" + name + "> onDone",
                "    ) {",
                "        return create(null, onDone);",
                "    }",
                "",
                "    static " + callbacksClass(te) + " create(",
                "        " + AbstractCallbacks.class.getName() + "<?, ?> parent,",
                "        " + Consumer.class.getName() + "<" + name + "> onDone",
                "    ) {",
                "        return new " + callbacksClass(te) + "(parent, onDone);",
                "    }",
                "",
                "    private " + callbacksClass(te) + "(",
                "        " + AbstractCallbacks.class.getName() + "<?, ?> parent, ",
                "        " + Consumer.class.getName() + "<" + name + "> onDone",
                "    ) {",
                "        super(" + builderClass(te) + ".create(), parent, onDone);"
            );
            Gen.write(bw, te.getRecordComponents()
                .stream()
                .map(element ->
                    "        " + RecordAttribute.create(element, roots, enums).callbackHandler(te) + ";"
                )
                .toList());
            Gen.write(bw, "    }");
            Gen.write(bw, "}");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write callbacks for " + te, e);
        }
    }

    private void writeWriter(
        PackageElement pe,
        TypeElement te,
        JavaFileObject file,
        Set<? extends Element> roots,
        Set<? extends Element> enums,
        Types typeUtils
    ) {
        try (BufferedWriter bw = Gen.writer(file)) {
            Gen.write(
                bw,
                "package " + pe.getQualifiedName() + ";",
                "",
                "final class " + writerClass(te),
                "    extends " + AbstractObjectWriter.class.getName() + "<" + te.getQualifiedName() + "> {",
                "",
                "    protected " + FieldEvents.class.getName() + " doWrite(",
                "        " + te.getQualifiedName() + " " + Gen.variableName(te) + ", ",
                "        " + FieldEvents.class.getName() + " events",
                "    ) {",
                "        return events"
            );
            te.getRecordComponents()
                .stream()
                .map(element -> RecordAttribute.create(element, roots, enums))
                .forEach(recordAttribute ->
                    Gen.write(
                        bw,
                        "            ." + recordAttribute.writeCall(te, typeUtils)
                    ));
            Gen.write(
                bw,
                "        ;",
                "    }",
                "}"
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write callbacks for " + te, e);
        }

    }

    private void writeRW(
        PackageElement pe,
        TypeElement te,
        JavaFileObject file,
        boolean read,
        boolean write
    ) {
        Name name = te.getQualifiedName();
        try (BufferedWriter bw = Gen.writer(file)) {
            Gen.write(
                bw,
                "package " + pe.getQualifiedName() + ";",
                "",
                "public final class " + rwClass(te) + " extends " + AbstractJsonRW.class.getName() + "<",
                "    " + name + ",",
                "    " + callbacksClass(te),
                "> {",
                "",
                "    public static " + rwClass(te) + " INSTANCE = new " + rwClass(te) + "();",
                "",
                "    private " + rwClass(te) + "() {",
                "        super(",
                "            " + name + ".class,",
                "            " + (read ? callbacksClass(te) + "::create" : "null") + ",",
                "            " + (write ? "new " + writerClass(te) + "()" : "null"),
                "        );",
                "    }",
                "}"
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write factory for " + te, e);
        }

    }

    private static final String JS_REC = JsonRecord.class.getName();

    private static String rwClass(TypeElement te) {
        JsonRecord rec = te.getAnnotation(JsonRecord.class);
        String name = te.getSimpleName().toString();
        return rec == null || rec.factoryClass().isBlank()
            ? name + "RW"
            : rec.factoryClass();
    }

    private static boolean isRecord(Element element) {
        return kind(element, ElementKind.RECORD) &&
               element instanceof TypeElement typeElement &&
               typeElement.getEnclosingElement() instanceof PackageElement;
    }

    private static Set<? extends Element> enums(RoundEnvironment roundEnv) {
        return Gen.enums(roundEnv.getRootElements())
            .collect(Collectors.toSet());
    }

    private static Set<? extends Element> types(RoundEnvironment roundEnv, Set<? extends Element> enums) {
        return roundEnv.getRootElements()
            .stream()
            .filter(element ->
                !enums.contains(element) && element.getAnnotation(JsonRecord.class) != null)
            .collect(Collectors.toSet());
    }

    private static boolean isJsonRecord(Set<? extends TypeElement> typedElements) {
        return typedElements != null && typedElements.stream()
            .anyMatch(JsonRecordProcessor::isJsonRecord);
    }

    private static boolean isRoot(TypeElement typeElement) {
        return typeElement.getAnnotation(JsonRecord.class).root();
    }

    private static String write(Set<? extends Element> roots) {
        return roots.stream()
            .map(Element::getSimpleName)
            .map(Objects::toString)
            .collect(Collectors.joining(", "));
    }

    private static boolean isJsonRecord(TypeElement typeElement) {
        return typeElement.getQualifiedName().toString().equals(JS_REC);
    }

    private static boolean kind(Element e, ElementKind... kinds) {
        return Arrays.asList(kinds).contains(e.getKind());
    }

}
