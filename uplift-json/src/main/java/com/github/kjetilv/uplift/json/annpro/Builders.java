package com.github.kjetilv.uplift.json.annpro;

import javax.lang.model.element.*;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Builders extends Gen {

    static void writeBuilder(
        PackageElement pe,
        TypeElement te,
        JavaFileObject file,
        Set<? extends Element> roots,
        Set<? extends Element> enums
    ) {
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
                listType(element, roots, enums).flatMap(listType ->
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
            "    public " + te.asType() + " get() {",
            "        return new " + te.asType() + "("
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
            write(bw, "package " + pe.getQualifiedName() + ";");
            write(bw, "");
            write(
                bw,
                "public final class " + builderClass(te) +
                " implements " + Supplier.class.getName() + "<" + te.getSimpleName() + ">{",
                "",
                "    public static " + builderClass(te) + " create() {",
                "        return new " + builderClass(te) + "();",
                "    }",
                "",
                "    private " + builderClass(te) + "() {",
                "    }",
                ""
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

    private Builders() {
    }

    private static Stream<String> listType(
        RecordComponentElement element,
        Set<? extends Element> rootElements,
        Set<? extends Element> enums
    ) {
        Optional<Class<?>> primitiveListType = primitiveListType(element.asType().toString());
        if (primitiveListType.isPresent()) {
            return primitiveListType.stream()
                .map(Class::getName);
        }
        Optional<? extends Element> enumListType = enumListType(element, enums);
        if (enumListType.isPresent()) {
            return enumListType.stream()
                .map(el -> el.asType().toString());
        }
        Optional<? extends Element> generatedListType = rootElements.stream()
            .filter(rootElement -> element.asType().toString().equals(listType(rootElement)))
            .findFirst();
        if (generatedListType.isPresent()) {
            return generatedListType.stream()
                .map(el -> el.asType().toString());
        }
        return Stream.empty();
    }
}
