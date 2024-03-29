package com.github.kjetilv.uplift.json.annpro;

import com.github.kjetilv.uplift.json.JsonRW;
import com.github.kjetilv.uplift.json.ObjectWriter;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.util.function.Consumer;
import java.util.function.Function;

final class RWs extends Gen {

    public static void writeRW(PackageElement pe, TypeElement te, JavaFileObject file) {
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
                "    implements " + JsonRW.class.getName() + "<",
                "        " + name + ",",
                "        " + packageElement(te) + "." + callbacksClassPlain(te),
                "    > {",
                "",
                "    public static com.github.kjetilv.uplift.json.JsonRW<",
                "        " + name + ",",
                "        " + packageElement(te) + "." + callbacksClassPlain(te),
                "    > INSTANCE = new " + factoryClass(te) + "();",
                "",
                "    @Override",
                "    public " + Function.class.getName() + "< ",
                "        " + Consumer.class.getName() + "<" + name + ">,",
                "        " + packageElement(te) + "." + callbacksClassPlain(te),
                "    > callbacks() {",
                "        return " + callbacksClassPlain(te) + "::create;",
                "    }",
                "",
                "    @Override",
                "    public " + packageElement(te) + "." + callbacksClassPlain(te) + " callbacks(",
                "        " + Consumer.class.getName() + "<" + name + "> onDone",
                "    ) {",
                "        return " + packageElement(te) + "." + callbacksClassPlain(te) + ".create(onDone);",
                "    }",
                "",
                "    @Override",
                "    public " + ObjectWriter.class.getName() + "<" + name + "> objectWriter() {",
                "        return new " + writerClassPlain(te) + "();",
                "    }",
                "",
                "    private " + factoryClass(te) + "() {",
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
