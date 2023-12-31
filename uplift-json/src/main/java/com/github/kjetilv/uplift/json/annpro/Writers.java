package com.github.kjetilv.uplift.json.annpro;

import com.github.kjetilv.uplift.json.AbstractObjectWriter;
import com.github.kjetilv.uplift.json.WriteEvents;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.util.Optional;
import java.util.Set;

final class Writers extends Gen {

    public static void writeWriter(
        PackageElement pe,
        TypeElement te,
        JavaFileObject file,
        Set<? extends Element> roots,
        Set<? extends Element> enums
    ) {
        try (BufferedWriter bw = writer(file)) {
            write(
                bw,
                "package " + pe.getQualifiedName() + ";",
                "",
                "final class " + writerClass(te),
                "    extends " + AbstractObjectWriter.class.getName() + "<" + te.getQualifiedName() + "> {",
                "",
                "    protected " + WriteEvents.class.getName() + " doWrite(",
                "        " + te.getQualifiedName() + " " + variableName(te) + ", ",
                "        " + WriteEvents.class.getName() + " events",
                "    ) {",
                "        return events"
            );
            te.getRecordComponents()
                .forEach(element ->
                    write(
                        bw,
                        "            ." + writeCall(te, element, roots, enums)
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

    private Writers() {
    }

    private static String writeCall(
        TypeElement te, RecordComponentElement element,
        Set<? extends Element> roots,
        Set<? extends Element> enums
    ) {
        Optional<String> listType = listType(element, roots, enums);
        boolean isEnum = isType(element, enums) || isListType(element, enums);
        boolean isRoot = isType(element, roots) || isListType(element,roots);
        boolean convert = isEnum || !isRoot && listType.map(BaseType::of).orElseGet(() -> BaseType.of(element)).requiresConversion();
        String name = isRoot ? "object"
            : isEnum ? "string"
            : listType.map(BaseType::of).orElseGet(() -> BaseType.of(element)).methodName();
        return name +
               listType.map(value -> "Array").orElse("") +
               "Field(" +
               "\"" + element.getSimpleName() + "\", " +
               variableName(te) + "." + element.getSimpleName() + "()" +
               (convert ? ", this::value)"
                   : isRoot ? ", new " + writerClass(listType.orElseGet(() -> element.asType().toString())) + "())"
                       : ")");
    }
}
