package com.github.kjetilv.uplift.json.annpro;

import com.github.kjetilv.uplift.json.events.AbstractJsonRW;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.util.function.Consumer;
import java.util.function.Function;

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
                "@" + Generated.class.getName() + "(",
                "    value = \"" + JsonRecordProcessor.class.getName() + "\",",
                "    date = \"" + time() + "\"",
                ")",
                "public final class " + factoryClass(te),
                "    extends " + AbstractJsonRW.class.getName() + "<",
                "        " + name + ",",
                "        " + callbacksClassQ(te),
                "    > {",
                "",
                "    public static com.github.kjetilv.uplift.json.JsonRW<",
                "        " + name + ",",
                "        " + callbacksClassQ(te),
                "    > INSTANCE = new " + factoryClass(te) + "();",
                "",
                "    public static " + Function.class.getName() + "< ",
                "        " + Consumer.class.getName() + "<" + name + ">,",
                "        " + callbacksClassQ(te),
                "    > callbacks() {",
                "        return " + callbacksClassQ(te) + "::create;",
                "    }",
                "",
                "    @Override",
                "    public " + callbacksClassQ(te) + " callbacks(",
                "        " + Consumer.class.getName() + "<" + name + "> onDone",
                "    ) {",
                "        return " + callbacksClassQ(te) + ".create(onDone);",
                "    }",
                "",
                "    private " + factoryClass(te) + "() {",
                "        super(",
                "            " + name + ".class,",
                "            " + (read ? callbacksClassQ(te) + "::create" : "null") + ",",
                "            " + (write ? "new " + writerClassQ(te) + "()" : "null"),
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
