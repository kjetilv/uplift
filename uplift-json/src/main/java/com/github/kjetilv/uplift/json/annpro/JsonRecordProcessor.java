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
        return isJsonRecord(typedElements) && processed(roundEnv);
    }

    private boolean processed(RoundEnvironment roundEnv) {
        Set<? extends Element> enums = enums(roundEnv);
        Set<? extends Element> roots = roots(roundEnv, enums);
        verifyRooted(roots);
        generate(roots, enums);
        return true;
    }

    private static Set<? extends Element> enums(RoundEnvironment roundEnv) {
        Set<? extends Element> enums = Gen.enums(roundEnv.getRootElements())
            .collect(Collectors.toSet());
        return enums;
    }

    private static Set<? extends Element> roots(RoundEnvironment roundEnv, Set<? extends Element> enums) {
        return roundEnv.getRootElements()
            .stream()
            .filter(element -> !enums.contains(element))
            .collect(Collectors.toSet());
    }

    private void generate(Set<? extends Element> roots, Set<? extends Element> enums) {
        roots.forEach(element ->
            process(element, roots, enums));
    }

    private void process(Element e, Set<? extends Element> roots, Set<? extends Element> enums) {
        if (kind(e, ElementKind.RECORD) &&
            e instanceof TypeElement typeElement &&
            typeElement.getEnclosingElement() instanceof PackageElement packageElement
        ) {
            Builders.writeBuilder(
                packageElement,
                typeElement,
                builderFile(typeElement),
                roots,
                enums
            );
            Callbacks.writeCallbacks(
                packageElement,
                typeElement,
                callbackFile(typeElement),
                roots,
                enums
            );
            if (isRoot(typeElement)) {
                Factories.writeFactory(
                    packageElement,
                    typeElement,
                    factoryFile(typeElement)
                );
            }
        }
    }

    private JavaFileObject factoryFile(TypeElement typeElement) {
        return file(Gen.factoryClass(typeElement));
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
            throw new RuntimeException(e);
        }
    }

    private static final String JS_REC = JsonRecord.class.getName();

    private static boolean isJsonRecord(Set<? extends TypeElement> typedElements) {
        return typedElements != null && typedElements.stream()
            .anyMatch(JsonRecordProcessor::isJsonRecord);
    }

    private static boolean isRoot(TypeElement typeElement) {
        return typeElement.getAnnotation(JsonRecord.class).root();
    }

    private static void verifyRooted(Set<? extends Element> roots) {
        if (
            roots.stream()
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .noneMatch(JsonRecordProcessor::isRoot)
        ) {
            throw new IllegalStateException(
                "None of " + roots.size() + " elements are root class(es): " +
                roots.stream()
                    .map(Element::getSimpleName)
                    .map(Objects::toString)
                    .collect(Collectors.joining(", ")));
        }
    }

    private static boolean isJsonRecord(TypeElement typeElement) {
        return typeElement.getQualifiedName().toString().equals(JS_REC);
    }

    private static boolean kind(Element e, ElementKind... kinds) {
        return Arrays.asList(kinds).contains(e.getKind());
    }

}
