package com.github.kjetilv.uplift.json.annpro;

import com.github.kjetilv.uplift.json.events.AbstractCallbacks;

import javax.annotation.processing.Generated;
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
                "public final class " + callbacksClassPlain(te),
                "    extends " + AbstractCallbacks.class.getName() + "<",
                "        " + packageElement(te) + "." + builderClassPlain(te) + ",",
                "        " + name,
                "    > {",
                "",
                "    public static " + callbacksClassPlain(te) + " create(",
                "        " + Consumer.class.getName() + "<" + name + "> onDone",
                "    ) {",
                "        return create(null, onDone);",
                "    }",
                "",
                "    static " + callbacksClassPlain(te) + " create(",
                "        " + AbstractCallbacks.class.getName() + "<?, ?> parent,",
                "        " + Consumer.class.getName() + "<" + name + "> onDone",
                "    ) {",
                "        return new " + packageElement(te) + "." + callbacksClassPlain(te) + "(parent, onDone);",
                "    }",
                "",
                "    " + callbacksClassPlain(te) + "(",
                "        " + AbstractCallbacks.class.getName() + "<?, ?> parent, ",
                "        " + Consumer.class.getName() + "<" + name + "> onDone",
                "    ) {",
                "        super(" + packageElement(te) + "." + builderClassPlain(te) + ".create(), parent, onDone);"
            );
            write(bw, te.getRecordComponents()
                .stream()
                .filter(recordComponentElement ->
                    recordComponentElement.getKind() == ElementKind.RECORD_COMPONENT)
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
