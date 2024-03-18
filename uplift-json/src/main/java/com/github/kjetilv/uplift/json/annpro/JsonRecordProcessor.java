package com.github.kjetilv.uplift.json.annpro;

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
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SupportedAnnotationTypes("com.github.kjetilv.uplift.json.anno.*")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public final class JsonRecordProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> typedElements, RoundEnvironment roundEnv) {
        if (isJsonRecord(typedElements)) {
            Set<? extends Element> enums = enums(roundEnv);
            Set<? extends Element> types = types(roundEnv, enums);
            return extracted(types, enums);
        }
        return false;
    }

    private boolean extracted(
        Set<? extends Element> types,
        Set<? extends Element> enums
    ) {
        if (types.isEmpty()) {
            if (enums.isEmpty()) {
                return true;
            }
            throw new IllegalStateException("No types for " + enums.size() + " enums: " + write(enums));
        }
        if (types.stream().allMatch(JsonRecordProcessor::isRecord)) {
            if (hasRoots(types)) {
                types.forEach(element ->
                    process(element, types, enums));
            } else {
                throw new IllegalStateException(
                    "None of " + types.size() + " elements are roots: " + write(types));
            }
        } else {
            throw new IllegalStateException("Only top-level record types are supported: " + write(types));
        }
        return false;
    }

    private void process(Element e, Set<? extends Element> roots, Set<? extends Element> enums) {
        if (e instanceof TypeElement te) {
            PackageElement pe = Gen.packageElement(te);
            JsonRecord jsonRecord = te.getAnnotation(JsonRecord.class);
            Types typeUtils = processingEnv.getTypeUtils();
            Builders.writeBuilder(pe, te, builderFile(te), roots, enums, typeUtils);
            Callbacks.writeCallbacks(pe, te, callbackFile(te), roots, enums);
            Writers.writeWriter(pe, te, writerFile(te), roots, enums, typeUtils);
            if (isRoot(te)) {
                RWs.writeRW(pe, te, factoryFile(pe, te));
            }
        } else {
            throw new IllegalStateException("Not a supported type: " + e);
        }
    }

    private JavaFileObject factoryFile(PackageElement pe, TypeElement te) {
        return file(Gen.factoryClassQ(pe, te));
    }

    private JavaFileObject callbackFile(TypeElement typeElement) {
        return file(typeElement, "Callbacks");
    }

    private JavaFileObject writerFile(TypeElement typeElement) {
        return file(typeElement, "Writer");
    }

    private JavaFileObject builderFile(TypeElement typeElement) {
        return file(typeElement, "Builder");
    }

    private JavaFileObject file(TypeElement te, String type) {
        return file(Gen.simpleName(te) + '_' + type);
    }

    private JavaFileObject file(String name) {
        try {
            return processingEnv.getFiler().createSourceFile(name);
        } catch (Exception e) {
            throw new IllegalStateException("Could not open file " + name, e);
        }
    }

    private static final String JSON_RECORD = JsonRecord.class.getName();

    private static boolean isJsonRecord(Set<? extends TypeElement> typedElements) {
        return Stream.ofNullable(typedElements)
            .flatMap(Collection::stream)
            .anyMatch(typeElement ->
                typeElement.getQualifiedName().toString().equals(JSON_RECORD));
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

    private static Set<? extends Element> enums(RoundEnvironment roundEnv) {
        return Gen.enums(roundEnv.getRootElements())
            .collect(Collectors.toSet());
    }

    private static Set<? extends Element> types(RoundEnvironment roundEnv, Set<? extends Element> enums) {
        return typeStream(enums, roundEnv.getRootElements()
            .stream())
            .collect(Collectors.toSet());
    }

    private static Stream<? extends Element> typeStream(
        Set<? extends Element> enums,
        Stream<? extends Element> stream
    ) {
        return stream
            .filter(element ->
                element.getAnnotation(JsonRecord.class) != null)
            .filter(element ->
                !enums.contains(element))
            .flatMap(element ->
                Stream.concat(
                    Stream.of(element),
                    typeStream(
                        enums,
                        element.getEnclosedElements()
                            .stream()
                            .filter(enc -> element.getKind().isClass())
                    )
                ));
    }

    private static boolean hasRoots(Set<? extends Element> roots) {
        return roots.stream()
            .filter(TypeElement.class::isInstance)
            .map(TypeElement.class::cast)
            .anyMatch(JsonRecordProcessor::isRoot);
    }

    private static boolean isRoot(TypeElement typeElement) {
        return typeElement.getAnnotation(JsonRecord.class).root();
    }

    private static String write(Set<? extends Element> roots) {
        return roots.stream()
            .map(Element::getSimpleName)
            .map(Objects::toString)
            .collect(Collectors.joining(", "));
    }

    private static boolean kind(Element e, ElementKind... kinds) {
        return Set.of(kinds).contains(e.getKind());
    }
}
