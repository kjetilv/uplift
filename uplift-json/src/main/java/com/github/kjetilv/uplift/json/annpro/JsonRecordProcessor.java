package com.github.kjetilv.uplift.json.annpro;

import com.github.kjetilv.uplift.json.anno.JsonRecord;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.JavaFileObject;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.kjetilv.uplift.json.annpro.Gen.enums;

@SupportedAnnotationTypes("com.github.kjetilv.uplift.json.anno.*")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class JsonRecordProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> typedElements, RoundEnvironment roundEnv) {
        return isJsRec(typedElements) && processed(roundEnv);
    }

    private boolean processed(RoundEnvironment roundEnv) {
        Set<? extends Element> enums = enums(roundEnv.getRootElements())
            .collect(Collectors.toSet());
        Set<? extends Element> roots = roundEnv.getRootElements()
            .stream()
            .filter(element -> !enums.contains(element))
            .collect(Collectors.toSet());
        roots.forEach(element ->
            process(element, roots, enums));
        return true;
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
            Factories.writeFactory(
                packageElement,
                typeElement,
                factoryFile(typeElement),
                roots,
                enums
            );
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

    private static boolean isJsRec(Set<? extends TypeElement> typedElements) {
        return typedElements != null && typedElements.stream()
            .anyMatch(JsonRecordProcessor::isJsRec);
    }

    private static boolean isJsRec(TypeElement typeElement) {
        return typeElement.getQualifiedName().toString().equals(JS_REC);
    }

    private static boolean kind(Element e, ElementKind... kinds) {
        return Arrays.asList(kinds).contains(e.getKind());
    }

}
