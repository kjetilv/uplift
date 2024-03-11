package com.github.kjetilv.uplift.json.annpro;

import com.github.kjetilv.uplift.json.events.AbstractJsonRW;

import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;

final class RWs extends Gen {

    public static void writeRW(
        PackageElement pe,
        TypeElement te,
        JavaFileObject file,
        boolean read,
        boolean write
    ) {
        Name name = te.getQualifiedName();
        try (BufferedWriter bw = writer(file)) {
            write(
                bw,
                "package " + pe.getQualifiedName() + ";",
                "",
                "public final class " + factoryClass(te) + " extends " + AbstractJsonRW.class.getName() + "<",
                "    " + name + ",",
                "    " + callbacksClass(te),
                "> {",
                "",
                "    public static com.github.kjetilv.uplift.json.JsonRW<",
                "        " + name + ",",
                "        " + callbacksClass(te),
                "    > INSTANCE = new " + factoryClass(te) + "();",
                "",
                "    private " + factoryClass(te) + "() {",
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

    private RWs() {

    }
}
