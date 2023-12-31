package com.github.kjetilv.uplift.json.annpro;

import com.github.kjetilv.uplift.json.anno.Field;
import com.github.kjetilv.uplift.json.anno.JsonRecord;
import com.github.kjetilv.uplift.json.anno.Singular;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

abstract sealed class Gen permits Builders, Callbacks, RWs, Writers {

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

    static String writerClass(TypeElement te) {
        return te.getSimpleName() + "Writer";
    }

    static String writerClass(Object te) {
        return te + "Writer";
    }

    static String factoryClass(TypeElement te) {
        JsonRecord rec = te.getAnnotation(JsonRecord.class);
        String name = te.getSimpleName().toString();
        return rec == null || rec.factoryClass().isBlank()
            ? name + DEFAULT_SUFFIX
            : rec.factoryClass();
    }

    static String variableName(TypeElement typeElement) {
        String name = typeElement.getSimpleName().toString();
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    static String setter(RecordComponentElement el) {
        return "set" + upcased(el.getSimpleName().toString());
    }

    static String adder(RecordComponentElement el) {
        return "add" + upcased(singularVariableName(el));
    }

    static Optional<RecordComponentElement> enumType(RecordComponentElement element, Set<? extends Element> enums) {
        return isType(element, enums)
            ? Optional.of(element)
            : Optional.empty();
    }

    static boolean isType(RecordComponentElement element, Set<? extends Element> candidates) {
        TypeMirror elementType = element.asType();
        return candidates.stream()
            .map(Element::asType)
            .anyMatch(elementType::equals);
    }

    static boolean isListType(RecordComponentElement element, Set<? extends Element> candidates) {
        TypeMirror elementType = element.asType();
        return candidates.stream()
            .map(Element::asType)
            .anyMatch(type ->
                listType(type).equals(elementType.toString()));
    }

    static Optional<? extends Element> enumListType(RecordComponentElement element, Set<? extends Element> enums) {
        return enums.stream()
            .filter(type ->
                listType(type).equals(element.asType().toString()))
            .findFirst();
    }

    static Optional<Class<?>> primitiveEvent(String list) {
        return Arrays.stream(BaseType.values())
            .filter(baseType ->
                baseType.fieldTypes()
                    .stream().anyMatch(fieldType -> fieldType.getName().equals(list)))
            .flatMap(baseType ->
                baseType.fieldTypes()
                    .stream())
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
        return Arrays.stream(BaseType.values())
            .filter(el ->
                el.fieldTypes().stream().anyMatch(fieldType -> list.equals(listType(fieldType))))
            .flatMap(baseType -> baseType.fieldTypes().stream())
            .findFirst();
    }

    private static final String DEFAULT_SUFFIX = "RW";

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

    protected static String listType(Object type) {
        return List.class.getName() + "<" + type + ">";
    }

    protected static Optional<String> listType(
        RecordComponentElement element,
        Set<? extends Element> rootElements,
        Set<? extends Element> enums
    ) {
        Optional<Class<?>> primitiveListType = primitiveListType(element.asType().toString());
        if (primitiveListType.isPresent()) {
            return primitiveListType
                .map(Class::getName);
        }
        Optional<? extends Element> enumListType = enumListType(element, enums);
        if (enumListType.isPresent()) {
            return enumListType
                .map(el -> el.asType().toString());
        }
        Optional<? extends Element> generatedListType = rootElements.stream()
            .filter(rootElement -> element.asType().toString().equals(listType(rootElement)))
            .findFirst();
        if (generatedListType.isPresent()) {
            return generatedListType
                .map(el -> el.asType().toString());
        }
        return Optional.empty();
    }

    private static String upcased(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }
}
