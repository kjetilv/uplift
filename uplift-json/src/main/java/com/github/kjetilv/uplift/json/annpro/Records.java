package com.github.kjetilv.uplift.json.annpro;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Records {

    static Optional<Records> mapFields(Set<? extends Element> types, Types typeUtils) {
        List<? extends Element> enums = enums(types).toList();
        List<? extends Element> recordTypes = types.stream()
            .filter(type -> !enums.contains(type))
            .toList();
        if (recordTypes.isEmpty()) {
            if (enums.isEmpty()) {
                return Optional.empty();
            }
            throw new IllegalStateException("No types for " + enums.size() + " enums: " + write(enums));
        }

        Map<TypeMirror, RecordFields> records = recordTypes
            .stream()
            .collect(Collectors.toMap(
                Element::asType,
                type ->
                    RecordFields.mapFields(type, typeUtils),
                (m1, m2) -> {
                    throw new IllegalStateException("No merge: " + m1 + " / " + m2);
                },
                LinkedHashMap::new
            ));
        return Optional.of(new Records(records, enums));
    }

    private final Map<TypeMirror, RecordFields> fields;

    private final List<? extends Element> enums;

    public Records(Map<TypeMirror, RecordFields> records, List<? extends Element> enums) {
        this.fields = records;
        this.enums = enums;
    }

    public List<? extends Element> enums() {
        return enums;
    }

    public RecordFields get(TypeElement typeElement) {
        return get(Objects.requireNonNull(typeElement, "typeElement").asType());
    }

    public RecordFields get(TypeMirror mirror) {
        RecordFields map = fields.get(mirror);
        if (map == null) {
            throw new IllegalArgumentException("No type " + mirror + " is known: " + fields.keySet());
        }
        return map;
    }

    public RecordField get(TypeMirror mirror, String field) {
        return get(mirror).get(field);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + fields.keySet() + "]";
    }

    public Collection<TypeMirror> types() {
        return fields.keySet();
    }

    private static Stream<? extends Element> enums(Set<? extends Element> types) {
        Predicate<Element> isEnum = element -> element.getKind() == ElementKind.ENUM;
        return Stream.of(
                types.stream()
                    .filter(isEnum),
                types.stream()
                    .filter(isEnum.negate())
                    .flatMap(type ->
                        enums(new HashSet<>(type.getEnclosedElements())))
            )
            .flatMap(Function.identity())
            .map(TypeElement.class::cast);
    }
}
