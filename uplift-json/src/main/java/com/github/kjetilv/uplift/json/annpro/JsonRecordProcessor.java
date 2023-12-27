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
import javax.tools.JavaFileObject;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("com.github.kjetilv.uplift.json.anno.*")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public final class JsonRecordProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> typedElements, RoundEnvironment roundEnv) {
        if (isJsonRecord(typedElements)) {
            Set<? extends Element> enums = enums(roundEnv);
            Set<? extends Element> types = roots(roundEnv, enums);
            if (types.isEmpty()) {
                if (enums.isEmpty()) {
                    return false;
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
        }
        return true;
    }

    private void process(Element e, Set<? extends Element> roots, Set<? extends Element> enums) {
        if (e instanceof TypeElement te && te.getEnclosingElement() instanceof PackageElement pe) {
            Builders.writeBuilder(pe, te, builderFile(te), roots, enums);
            Callbacks.writeCallbacks(pe, te, callbackFile(te), roots, enums);
            if (isRoot(te)) {
                Factories.writeFactory(pe, te, factoryFile(pe, te));
            }
        } else {
            throw new IllegalStateException("Not a supported type: " + e);
        }
    }

    private JavaFileObject factoryFile(PackageElement pe, TypeElement te) {
        return file(pe.getQualifiedName() + "." + Gen.factoryClass(te));
    }

    private JavaFileObject callbackFile(TypeElement typeElement) {
        return file(typeElement, "Callbacks");
    }

    private JavaFileObject builderFile(TypeElement typeElement) {
        return file(typeElement, "Builder");
    }

    private JavaFileObject file(TypeElement te, String type) {
        return file(te.getQualifiedName() + type);
    }

    private JavaFileObject file(String name) {
        try {
            return processingEnv.getFiler().createSourceFile(name);
        } catch (Exception e) {
            throw new IllegalStateException("Could not open file " + name, e);
        }
    }

    private static final String JS_REC = JsonRecord.class.getName();

    private static boolean isRecord(Element element) {
        return kind(element, ElementKind.RECORD) &&
               element instanceof TypeElement typeElement &&
               typeElement.getEnclosingElement() instanceof PackageElement;
    }

    private static Set<? extends Element> enums(RoundEnvironment roundEnv) {
        return Gen.enums(roundEnv.getRootElements())
            .collect(Collectors.toSet());
    }

    private static Set<? extends Element> roots(RoundEnvironment roundEnv, Set<? extends Element> enums) {
        return roundEnv.getRootElements()
            .stream()
            .filter(element -> !enums.contains(element))
            .collect(Collectors.toSet());
    }

    private static boolean isJsonRecord(Set<? extends TypeElement> typedElements) {
        return typedElements != null && typedElements.stream()
            .anyMatch(JsonRecordProcessor::isJsonRecord);
    }

    private static boolean isRoot(TypeElement typeElement) {
        return typeElement.getAnnotation(JsonRecord.class).root();
    }

    private static boolean hasRoots(Set<? extends Element> roots) {
        return roots.stream()
            .filter(TypeElement.class::isInstance)
            .map(TypeElement.class::cast)
            .anyMatch(JsonRecordProcessor::isRoot);
    }

    private static String write(Set<? extends Element> roots) {
        return roots.stream()
            .map(Element::getSimpleName)
            .map(Objects::toString)
            .collect(Collectors.joining(", "));
    }

    private static boolean isJsonRecord(TypeElement typeElement) {
        return typeElement.getQualifiedName().toString().equals(JS_REC);
    }

    private static boolean kind(Element e, ElementKind... kinds) {
        return Arrays.asList(kinds).contains(e.getKind());
    }

}
