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

@SupportedAnnotationTypes("com.github.kjetilv.uplift.json.anno.*")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public final class JsonRecordProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment roundEnv) {
        if (containsJsonRecord(typeElements)) {
            Collection<? extends Element> typeEls = typeEls(roundEnv);
            Collection<? extends Element> enumEls = enumEls(roundEnv);
            if (typeEls.isEmpty()) {
                requireEmptyEnums(enumEls);
                return true;
            }
            requireAllRecords(typeEls);
            requireAnyRootType(typeEls);

            write(typeEls, enumEls);
        }
        return false;
    }

    private void write(
        Collection<? extends Element> typeEls,
        Collection<? extends Element> enumEls
    ) {
        for (Element el : typeEls) {
            try {
                TypeElement typeEl = typeEl(el);
                Optional<TypeElement> rootEl = rootEl(typeEl);
                PackageElement packageEl = packageEl(typeEl);
                writeBuilder(
                    packageEl,
                    typeEl,
                    builderFile(typeEl),
                    typeEls,
                    enumEls
                );
                writeCallbacks(
                    packageEl,
                    typeEl,
                    callbackFile(typeEl),
                    typeEls,
                    enumEls,
                    rootEl.isPresent()
                );
                writeWriter(
                    packageEl,
                    typeEl,
                    writerFile(typeEl),
                    typeEls,
                    enumEls
                );
                rootEl.ifPresent(element ->
                    writeRW(packageEl, element, factoryFile(packageEl, element))
                );
            } catch (Exception e) {
                throw new IllegalStateException("Failed to write " + el, e);
            }
        }
    }

    private JavaFileObject factoryFile(PackageElement packageEl, TypeElement typeEl) {
        return file(factoryClassQ(packageEl, typeEl));
    }

    private JavaFileObject callbackFile(TypeElement typeEl) {
        return classFileName(typeEl, "Callbacks");
    }

    private JavaFileObject writerFile(TypeElement typeEl) {
        return classFileName(typeEl, "Writer");
    }

    private JavaFileObject builderFile(TypeElement typeEl) {
        return classFileName(typeEl, "Builder");
    }

    private JavaFileObject classFileName(TypeElement typeEl, String callbacks) {
        return file(fqName(typeEl) + '_' + callbacks);
    }

    private JavaFileObject file(String name) {
        try {
            return processingEnv.getFiler().createSourceFile(name);
        } catch (Exception e) {
            throw new IllegalStateException("Could not open file " + name, e);
        }
    }

    private static void requireAllRecords(Collection<? extends Element> els) {
        if (els.stream().anyMatch(JsonRecordProcessor::isNotRecord)) {
            throw new IllegalStateException(
                "Only top-level record types are supported, found " + print(
                    els,
                    JsonRecordProcessor::isNotRecord
                ));
        }
    }

    private static void requireEmptyEnums(Collection<? extends Element> els) {
        if (!els.isEmpty()) {
            throw new IllegalStateException(
                "No types for " + els.size() + " enums: " + print(els));
        }
    }

    private static void requireAnyRootType(Collection<? extends Element> els) {
        if (rootType(els).isEmpty()) {
            throw new IllegalStateException(
                "None of " + els.size() + " elements are roots: " + print(els));
        }
    }

    private static Optional<TypeElement> rootType(Collection<? extends Element> els) {
        return typeEls(els).flatMap(typeEl -> rootEl(typeEl).stream()).findAny();
    }

    private static TypeElement typeEl(Element el) {
        if (el instanceof TypeElement typeEl) {
            return typeEl;
        }
        throw new IllegalStateException("Not a supported type: " + el);
    }

    private static Stream<TypeElement> typeEls(Collection<? extends Element> typeEls) {
        return typeEls.stream()
            .filter(TypeElement.class::isInstance)
            .map(TypeElement.class::cast);
    }

    private static boolean containsJsonRecord(Set<? extends TypeElement> typeEls) {
        return Stream.ofNullable(typeEls)
            .flatMap(Collection::stream)
            .anyMatch(JsonRecordProcessor::isJsonRecord);
    }

    private static boolean isJsonRecord(TypeElement typeEl) {
        return typeEl.getQualifiedName().toString().equals(JsonRecord.class.getName());
    }

    private static boolean isNotRecord(Element element) {
        return !isRecordElement(element);
    }

    private static boolean isRecordElement(Element el) {
        return kind(el, ElementKind.RECORD) &&
               el instanceof TypeElement typeEl &&
               isPackageOrClass(typeEl.getEnclosingElement());
    }

    private static boolean isPackageOrClass(Element enclosingEl) {
        return enclosingEl instanceof PackageElement || enclosingEl instanceof TypeElement;
    }

    private static Collection<? extends Element> enumEls(RoundEnvironment roundEnv) {
        return Gen.enums(roundEnv.getRootElements())
            .collect(Collectors.toSet());
    }

    private static Collection<? extends Element> typeEls(RoundEnvironment roundEnv) {
        Stream<? extends Element> annotatedRecords = roundEnv.getRootElements()
            .stream()
            .filter(el ->
                el.getAnnotation(JsonRecord.class) != null);
        return typeStream(annotatedRecords)
            .collect(Collectors.toSet());
    }

    private static Stream<? extends Element> typeStream(Stream<? extends Element> els) {
        return els.flatMap(el ->
            Stream.concat(
                Stream.of(el),
                typeStream(el.getEnclosedElements()
                    .stream()
                    .filter(enc -> enc.getKind() == ElementKind.RECORD))
            ));
    }

    private static Optional<TypeElement> rootEl(TypeElement typeEl) {
        return Optional.of(typeEl).flatMap(el ->
            Optional.ofNullable(el.getAnnotation(JsonRecord.class))
                .filter(JsonRecord::root)
                .map(_ -> el));
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
