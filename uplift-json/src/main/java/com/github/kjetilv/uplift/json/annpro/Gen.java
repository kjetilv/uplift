package com.github.kjetilv.uplift.json.annpro;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class Gen {

    protected static Stream<? extends Element> enums(Set<? extends Element> rootElements) {
        return Stream.of(
                rootElements.stream()
                    .filter(element -> element.getKind() == ElementKind.ENUM),
                rootElements.stream().flatMap(el -> {
                    List<? extends Element> enclosedElements = el.getEnclosedElements();
                    return enums(new HashSet<>(enclosedElements));
                })
            )
            .flatMap(Function.identity());
    }

    protected static final Set<Class<?>> BASE_TYPES = Set.of(
        String.class,
        Boolean.class,
        Integer.class,
        Long.class,
        Double.class,
        Float.class,
        Short.class,
        Byte.class
    );

    protected static void write(BufferedWriter bw, List<String> strs) {
        strs.forEach(str -> {
            try {
                write(bw, str);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    protected static void write(BufferedWriter bw, String str) {
        try {
            bw.write(str + "\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static BufferedWriter writer(JavaFileObject builder) {
        try {
            return new BufferedWriter(builder.openWriter());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static String builderClass(TypeElement te) {
        return te.getSimpleName() + "Builder";
    }

    protected static String callbacksClass(TypeElement te) {
        return te.getSimpleName() + "Callbacks";
    }

    protected static String setter(RecordComponentElement el) {
        return "set" + upcased(el.getSimpleName().toString());
    }

    protected static String adder(RecordComponentElement el) {
        return "add" + upcased(el.getSimpleName().toString());
    }

    protected static String handler(RecordComponentElement el) {
        return "on" + upcased(el.getSimpleName().toString());
    }

    protected static Optional<RecordComponentElement> enumType(
        RecordComponentElement element,
        Set<? extends Element> enums
    ) {
        return enums.stream()
            .map(Element::asType).anyMatch(type -> type.equals(element.asType()))
            ? Optional.of(element)
            : Optional.empty();
    }

    protected static Optional<? extends Element> enumListType(
        RecordComponentElement element,
        Set<? extends Element> enums
    ) {
        return enums.stream()
            .filter(type ->
                ("java.util.List<" + type.asType() + ">").equals(element.asType().toString()))
            .findFirst();
    }

    protected static Optional<Class<?>> primitiveEvent(String list) {
        return BASE_TYPES.stream()
            .filter(type ->
                type.getName().equals(list))
            .findFirst();
    }

    protected static Optional<TypeElement> generatedEvent(String generated, Set<? extends Element> rootElements) {
        return rootElements.stream()
            .filter(el ->
                el instanceof TypeElement te && te.getQualifiedName().toString().equals(generated))
            .findFirst()
            .map(TypeElement.class::cast);
    }

    protected static Optional<TypeElement> generatedListType(String generatedList, Set<? extends Element> rootElements) {
        return rootElements.stream()
            .filter(el ->
                el instanceof TypeElement te && generatedList.equals("java.util.List<" + te.getQualifiedName() + ">"))
            .findFirst()
            .map(TypeElement.class::cast);
    }

    protected static Optional<Class<?>> primitiveListType(String list) {
        return BASE_TYPES.stream()
            .filter(el ->
                list.equals("java.util.List<" + el.getName() + ">"))
            .findFirst();
    }

    private static String upcased(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }
}
