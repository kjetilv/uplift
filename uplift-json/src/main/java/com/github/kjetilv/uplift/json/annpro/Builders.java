package com.github.kjetilv.uplift.json.annpro;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Builders extends Gen {

    static void writeBuilder(
        PackageElement pe,
        TypeElement te,
        JavaFileObject file,
        Set<? extends Element> roots,
        Set<? extends Element> enums,
        Types typeUtils
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
                listType(element, roots, enums, typeUtils)
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

        Name name = te.getQualifiedName();
        try (BufferedWriter bw = writer(file)) {
            write(bw, "package " + pe.getQualifiedName() + ";");
            write(bw, "");
            write(
                bw,
                "@" + Generated.class.getName() + "(",
                "    value = \"" + JsonRecordProcessor.class.getName() + "\",",
                "    date = \"" + time() + "\"",
                ")",
                "final class " + builderClassPlain(te),
                "    implements " + Supplier.class.getName() + "<",
                "        " + name,
                "    > {",
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

    private Builders() {
    }
}
