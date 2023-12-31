package com.github.kjetilv.uplift.json.annpro;

import com.github.kjetilv.uplift.json.events.AbstractCallbacks;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
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
                    "        " + attributeType(element, roots, enums).handler(te) + ";"
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

    private static AttributeType attributeType(
        RecordComponentElement element,
        Set<? extends Element> roots,
        Set<? extends Element> enums
    ) {
        return switch (element.asType().getKind()) {
            case BOOLEAN -> new AttributeType("Boolean", element);
            case BYTE -> new AttributeType("Byte", element);
            case SHORT -> new AttributeType("Short", element);
            case INT -> new AttributeType("Integer", element);
            case LONG -> new AttributeType("Long", element);
            case CHAR -> new AttributeType("Character", element);
            case FLOAT -> new AttributeType("Float", element);
            case DOUBLE -> new AttributeType("Double", element);
            case DECLARED -> resolveDeclared(element, roots, enums);
            default -> throw new IllegalStateException("Unsupported: " + element);
        };
    }

    private static AttributeType resolveDeclared(
        RecordComponentElement element,
        Set<? extends Element> roots,
        Set<? extends Element> enums
    ) {
        TypeMirror type = element.asType();
        String string = type.toString();
        return enumType(element, enums).map(enumType ->
                new AttributeType(
                    "Enum",
                    element,
                    AttributeType.Variant.ENUM,
                    null
                ))
            .or(() -> enumListType(element, enums).map(enumListType ->
                new AttributeType(
                    "Enum",
                    element,
                    AttributeType.Variant.ENUM_LIST,
                    (TypeElement) enumListType
                )))
            .or(() -> primitiveEvent(string).map(event ->
                new AttributeType(
                    event.getSimpleName(),
                    element,
                    AttributeType.Variant.PRIMITIVE,
                    null
                )))
            .or(() ->
                generatedEvent(string, roots)
                    .map(generatedType ->
                        new AttributeType(
                            "Object",
                            element,
                            AttributeType.Variant.GENERATED,
                            generatedType
                        )
                    ))
            .or(() ->
                primitiveListType(string)
                    .map(primtiveType ->
                        new AttributeType(
                            primtiveType.getSimpleName(),
                            element,
                            AttributeType.Variant.PRIMITIVE_LIST,
                            null
                        )
                    ))
            .or(() ->
                generatedListType(string, roots).map(generatedType ->
                    new AttributeType(
                        "Object",
                        element,
                        AttributeType.Variant.GENERATED_LIST,
                        generatedType
                    )
                ))
            .orElseThrow(() ->
                new IllegalStateException("Unsupported: " + element + ":" + string));
    }
}
