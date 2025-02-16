package com.github.kjetilv.uplift.json.gen;

import com.github.kjetilv.uplift.json.anno.JsonRecord;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.kjetilv.uplift.json.gen.Gen.*;

@SupportedAnnotationTypes(
    {
        "com.github.kjetilv.uplift.json.anno.JsonRecord",
        "com.github.kjetilv.uplift.json.anno.Field",
        "com.github.kjetilv.uplift.json.anno.Singular"
    }
)
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public final class JsonRecordProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> typedElements, RoundEnvironment roundEnv) {
        if (containsJsonRecord(typedElements)) {
            Collection<? extends Element> types = types(roundEnv);
            Collection<? extends Element> enums = enums(roundEnv);
            if (types.isEmpty()) {
                requireEmptyEnums(enums);
                return true;
            }
            requireAllRecords(types);
            requireAnyRootType(types);

            write(types, enums);
        }
        return false;
    }

    private void write(
        Collection<? extends Element> types,
        Collection<? extends Element> enums
    ) {
        for (Element el : types) {
            try {
                TypeElement typeElement = typeEl(el);
                Optional<TypeElement> rootElement = rootElement(typeElement);
                PackageElement packageEl = packageEl(typeElement);
                writeBuilder(packageEl, typeElement, builderFile(typeElement), types, enums);
                writeCallbacks(packageEl, typeElement, callbackFile(typeElement), types, enums, rootElement.isPresent());
                writeWriter(packageEl, typeElement, writerFile(typeElement), types, enums);
                rootElement.ifPresent(rootEl ->
                    writeRW(packageEl, rootEl, factoryFile(packageEl, rootEl))
                );
            } catch (Exception e) {
                throw new IllegalStateException("Failed to write " + el, e);
            }
        }
    }

    private JavaFileObject factoryFile(PackageElement pe, TypeElement te) {
        return file(factoryClassQ(pe, te));
    }

    private JavaFileObject callbackFile(TypeElement typeElement) {
        return classFileName(typeElement, "Callbacks");
    }

    private JavaFileObject writerFile(TypeElement typeElement) {
        return classFileName(typeElement, "Writer");
    }

    private JavaFileObject builderFile(TypeElement typeElement) {
        return classFileName(typeElement, "Builder");
    }

    private JavaFileObject classFileName(TypeElement typeElement, String callbacks) {
        return file(fqName(typeElement) + '_' + callbacks);
    }

    private JavaFileObject file(String name) {
        try {
            return processingEnv.getFiler().createSourceFile(name);
        } catch (Exception e) {
            throw new IllegalStateException("Could not open file " + name, e);
        }
    }

    private static final String JSON_RECORD = JsonRecord.class.getName();

    private static void requireAllRecords(Collection<? extends Element> types) {
        if (types.stream().anyMatch(JsonRecordProcessor::isNotRecord)) {
            throw new IllegalStateException(
                "Only top-level record types are supported, found " + print(
                    types,
                    JsonRecordProcessor::isNotRecord
                ));
        }
    }

    private static void requireEmptyEnums(Collection<? extends Element> enums) {
        if (!enums.isEmpty()) {
            throw new IllegalStateException(
                "No types for " + enums.size() + " enums: " + print(enums));
        }
    }

    private static void requireAnyRootType(Collection<? extends Element> types) {
        if (rootType(types).isEmpty()) {
            throw new IllegalStateException(
                "None of " + types.size() + " elements are roots: " + print(types));
        }
    }

    private static Optional<TypeElement> rootType(Collection<? extends Element> types) {
        return typeElements(types)
            .flatMap(typeElement ->
                rootElement(typeElement).stream())
            .findAny();
    }

    private static TypeElement typeEl(Element element) {
        if (element instanceof TypeElement typeElement) {
            return typeElement;
        }
        throw new IllegalStateException("Not a supported type: " + element);
    }

    private static Stream<TypeElement> typeElements(Collection<? extends Element> types) {
        return types.stream()
            .filter(TypeElement.class::isInstance)
            .map(TypeElement.class::cast);
    }

    private static boolean containsJsonRecord(Set<? extends TypeElement> typedElements) {
        return Stream.ofNullable(typedElements).flatMap(Collection::stream)
            .anyMatch(typeElement ->
                typeElement.getQualifiedName().toString().equals(JSON_RECORD));
    }

    private static boolean isNotRecord(Element element) {
        return !isRecord(element);
    }

    private static boolean isRecord(Element element) {
        return kind(element, ElementKind.RECORD) &&
               element instanceof TypeElement typeElement &&
               isPackageOrClass(typeElement.getEnclosingElement());
    }

    private static boolean isPackageOrClass(Element enclosingElement) {
        return enclosingElement instanceof PackageElement ||
               enclosingElement instanceof TypeElement;
    }

    private static Collection<? extends Element> enums(RoundEnvironment roundEnv) {
        return Gen.enums(roundEnv.getRootElements())
            .collect(Collectors.toSet());
    }

    private static Collection<? extends Element> types(RoundEnvironment roundEnv) {
        Stream<? extends Element> annotatedRecords = roundEnv.getRootElements()
            .stream()
            .filter(element ->
                element.getAnnotation(JsonRecord.class) != null);
        return typeStream(annotatedRecords)
            .collect(Collectors.toSet());
    }

    private static Stream<? extends Element> typeStream(Stream<? extends Element> stream) {
        return stream.flatMap(element ->
            Stream.concat(
                Stream.of(element),
                typeStream(element.getEnclosedElements()
                    .stream()
                    .filter(enc -> enc.getKind() == ElementKind.RECORD))
            ));
    }

    private static Optional<TypeElement> rootElement(TypeElement typeElement) {
        return Optional.of(typeElement).flatMap(element ->
            Optional.ofNullable(element.getAnnotation(JsonRecord.class))
                .filter(JsonRecord::root)
                .map(_ -> element));
    }

    private static <E extends Element> String print(Collection<E> roots) {
        return print(roots, null);
    }

    private static <E extends Element> String print(Collection<E> roots, Predicate<E> filter) {
        return roots.stream()
            .filter(filter == null ? _ -> true : filter)
            .map(Element::getSimpleName)
            .map(Objects::toString)
            .collect(Collectors.joining(", "));
    }

    private static boolean kind(Element e, ElementKind... kinds) {
        return Set.of(kinds).contains(e.getKind());
    }
}
