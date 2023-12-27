package com.github.kjetilv.uplift.json.annpro;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.util.List;
import java.util.Set;

final class Callbacks extends Gen {

    static void writeCallbacks(
        PackageElement pe,
        TypeElement te,
        JavaFileObject callbacks,
        Set<? extends Element> roots,
        Set<? extends Element> enums
    ) {
        List<String> header = List.of(
            "package " + pe.getQualifiedName() + ";",
            "",
            "import java.util.function.Consumer;",
            "",
            "import com.github.kjetilv.uplift.json.events.AbstractCallbacks;",
            ""
        );

        Name name = te.getSimpleName();
        try (BufferedWriter bw = writer(callbacks)) {
            write(bw, header);
            write(
                bw,
                "public final class " + callbacksClass(te) + " extends AbstractCallbacks<" + builderClass(te) + ", " + name + "> {"
            );
            write(bw, "");
            write(bw, "    public " + callbacksClass(te) + "(Consumer<" + name + "> onDone) {");
            write(bw, "        this(null, onDone);");
            write(bw, "    }");
            write(bw, "");
            write(
                bw,
                "    public " + callbacksClass(te) + "(AbstractCallbacks<?, ?> parent, Consumer<" + name + "> onDone) {"
            );
            write(bw, "        super(new " + builderClass(te) + "(), parent, onDone);");
            write(bw, te.getRecordComponents()
                .stream()
                .map(element -> {
                        AttributeType attributeType = attributeType(element, roots, enums);
                        String handler = attributeType.handler(te);
                        return "        " + handler + ";";
                    }
                )
                .toList());
            write(bw, "    }");
            write(bw, "}");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
                new IllegalStateException("Unsupported: " + string));
    }

    private Callbacks() {

    }
}
