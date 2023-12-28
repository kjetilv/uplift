package com.github.kjetilv.uplift.json.annpro;

import com.github.kjetilv.uplift.json.anno.Field;
import com.github.kjetilv.uplift.json.anno.JsonRecord;
import com.github.kjetilv.uplift.json.anno.Singular;
import com.github.kjetilv.uplift.uuid.Uuid;

import javax.lang.model.element.*;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

abstract sealed class Gen permits Builders, Callbacks, Factories {

    static String listType(TypeElement te) {
        return listType(te.getQualifiedName());
    }

    static String listType(Class<?> el) {
        return listType(el.getName());
    }

    static String listType(Element type) {
        return listType(type.asType());
    }

    static Stream<? extends Element> enums(Set<? extends Element> rootElements) {
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

    static void write(BufferedWriter bw, String... strs) {
        write(bw, Arrays.asList(strs));
    }

    static void write(BufferedWriter bw, List<String> strs) {
        strs.forEach(str -> {
            try {
                write(bw, str);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to write line `" + str + "`", e);
            }
        });
    }

    static void write(BufferedWriter bw, String str) {
        try {
            bw.write(str + "\n");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write line `" + str + "`", e);
        }
    }

    static BufferedWriter writer(JavaFileObject builder) {
        try {
            return new BufferedWriter(builder.openWriter());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write to " + builder, e);
        }
    }

    static String builderClass(TypeElement te) {
        return te.getSimpleName() + "Builder";
    }

    static String callbacksClass(TypeElement te) {
        return te.getSimpleName() + "Callbacks";
    }

    static String factoryClass(TypeElement te) {
        JsonRecord rec = te.getAnnotation(JsonRecord.class);
        String name = te.getSimpleName().toString();
        return rec == null || rec.factoryClass().isBlank()
            ? name + DEFAULT_SUFFIX
            : rec.factoryClass();
    }

    static String setter(RecordComponentElement el) {
        return "set" + upcased(el.getSimpleName().toString());
    }

    static String adder(RecordComponentElement el) {
        return "add" + upcased(singularVariableName(el));
    }

    static Optional<RecordComponentElement> enumType(
        RecordComponentElement element,
        Set<? extends Element> enums
    ) {
        return enums.stream()
            .map(Element::asType).anyMatch(type -> type.equals(element.asType()))
            ? Optional.of(element)
            : Optional.empty();
    }

    static Optional<? extends Element> enumListType(
        RecordComponentElement element,
        Set<? extends Element> enums
    ) {
        return enums.stream()
            .filter(type ->
                listType(type).equals(element.asType().toString()))
            .findFirst();
    }

    static Optional<Class<?>> primitiveEvent(String list) {
        return BASE_TYPES.stream()
            .filter(type ->
                type.getName().equals(list))
            .findFirst();
    }

    static Optional<TypeElement> generatedEvent(String generated, Set<? extends Element> rootElements) {
        return rootElements.stream()
            .filter(el ->
                el instanceof TypeElement te && te.getQualifiedName().toString().equals(generated))
            .findFirst()
            .map(TypeElement.class::cast);
    }

    static Optional<TypeElement> generatedListType(
        String generatedList,
        Set<? extends Element> rootElements
    ) {
        return rootElements.stream()
            .filter(el ->
                el instanceof TypeElement te && generatedList.equals(listType(te)))
            .findFirst()
            .map(TypeElement.class::cast);
    }

    static Optional<Class<?>> primitiveListType(String list) {
        return BASE_TYPES.stream()
            .filter(el ->
                list.equals(listType(el)))
            .findFirst();
    }

    private static final String DEFAULT_SUFFIX = "Factory";

    private static final Set<Class<?>> BASE_TYPES = Set.of(
        String.class,
        Boolean.class,
        Integer.class,
        Long.class,
        Double.class,
        Float.class,
        Short.class,
        Byte.class,
        BigDecimal.class,
        BigInteger.class,
        UUID.class,
        Uuid.class,
        Instant.class,
        Duration.class
    );

    protected static String singularVariableName(RecordComponentElement el) {
        Singular annotation = el.getAnnotation(Singular.class);
        String plural = fieldName(el);
        return annotation != null ? annotation.value()
            : plural.endsWith("s") ? plural.substring(0, plural.length() - 1)
                : plural;
    }

    protected static String fieldName(RecordComponentElement el) {
        Field field = el.getAnnotation(Field.class);
        return field == null ? el.getSimpleName().toString()
            : field.value();
    }

    private static String listType(Object type) {
        return List.class.getName() + "<" + type + ">";
    }

    private static String upcased(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }
}
