package com.github.kjetilv.uplift.json.annpro;

import com.github.kjetilv.uplift.json.events.AbstractFactory;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.util.Set;

final class Factories extends Gen {

    public static void writeFactory(
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
                "public final class " + factoryClass(te) + " extends " + AbstractFactory.class.getName() + "<" +
                builderClass(te) + ", " +
                name + ", " +
                callbacksClass(te) + "> {",
                "",
                "    public static " + factoryClass(te) + " INSTANCE = new " + factoryClass(te) + "();",
                "",
                "    private " + factoryClass(te) + "() {",
                "        super(" + callbacksClass(te) + "::create);",
                "    }",
                "}"
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private Factories() {

    }
}
