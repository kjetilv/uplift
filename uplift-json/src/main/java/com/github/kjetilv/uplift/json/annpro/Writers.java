package com.github.kjetilv.uplift.json.annpro;

import com.github.kjetilv.uplift.json.AbstractObjectWriter;
import com.github.kjetilv.uplift.json.FieldEvents;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
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
                "    protected " + FieldEvents.class.getName() + " doWrite(",
                "        " + te.getQualifiedName() + " " + variableName(te) + ", ",
                "        " + FieldEvents.class.getName() + " events",
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

    private Writers() {
    }

}
