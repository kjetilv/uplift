package com.github.kjetilv.uplift.json.annpro;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Builders extends Gen {

    static void writeBuilder(
        PackageElement pe,
        TypeElement te,
        JavaFileObject builder,
        Set<? extends Element> roots,
        Set<? extends Element> enums
    ) {
        List<String> header = List.of(
            "package " + pe.getQualifiedName() + ";",
            "",
            "import java.util.ArrayList;",
            "import java.util.List;",
            "import java.util.function.Supplier;"
        );

        List<String> setters = te.getRecordComponents()
            .stream().flatMap(el ->
                Stream.of(
                    "    private " + el.asType() + " " + el.getSimpleName() + ";",
                    "",
                    "    void " + setter(el) + "(" + el.asType() + " value) {",
                    "        this." + el.getSimpleName() + " = value;",
                    "    }",
                    ""
                ))
            .toList();

        List<String> adders = te.getRecordComponents()
            .stream()
            .flatMap(element ->
                listType(element, roots, enums).flatMap(listType ->
                    Stream.of(
                        "    void " + adder(element) + "(" + listType + " value) {",
                        "        if (this." + element.getSimpleName() + " == null) {",
                        "            this." + element.getSimpleName() + " = new ArrayList();",
                        "        }",
                        "        this." + element.getSimpleName() + ".add(value);",
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
                "            " + el.getSimpleName() + ",")
            .collect(Collectors.toCollection(LinkedList::new));
        String last = creatorMeat.removeLast();
        creatorMeat.addLast(last.substring(0, last.length() - 1));

        List<String> creatorEnd = List.of(
            "        );",
            "    }"
        );

        try (BufferedWriter bw = writer(builder)) {
            write(bw, header);
//                write(bw, imports);
            write(bw, "");
            write(
                bw,
                "public class " + builderClass(te) +
                " implements Supplier<" + te.getSimpleName() + ">{"
            );
            write(bw, "");
            write(bw, setters);
            write(bw, adders);
            write(bw, creatorStart);
            write(bw, creatorMeat);
            write(bw, creatorEnd);
            write(bw, "}");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    private Builders() {
    }
}
