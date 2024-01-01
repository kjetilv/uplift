package com.github.kjetilv.uplift.json.annpro;

import com.github.kjetilv.uplift.json.events.AbstractCallbacks;

import javax.lang.model.element.*;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.util.Set;
import java.util.function.Consumer;

final class Callbacks extends Gen {

    static void writeCallbacks(
        PackageElement pe,
        TypeElement te,
        JavaFileObject file,
        Set<? extends Element> roots,
        Set<? extends Element> enums
    ) {
        Name name = te.getSimpleName();
        try (BufferedWriter bw = writer(file)) {
            write(
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
            write(bw, te.getRecordComponents()
                .stream()
                .map(element ->
                    "        " + RecordAttribute.create(element, roots, enums).callbackHandler(te) + ";"
                )
                .toList());
            write(bw, "    }");
            write(bw, "}");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write callbacks for " + te, e);
        }
    }

    private Callbacks() {

    }

}
