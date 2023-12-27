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
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.kjetilv.uplift.json.annpro.Gen.enums;

@SupportedAnnotationTypes("com.github.kjetilv.uplift.json.anno.*")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class JsRecProcessor extends AbstractProcessor {

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
        if (kind(e, ElementKind.RECORD, ElementKind.CLASS) &&
            e instanceof TypeElement typeElement &&
            typeElement.getEnclosingElement() instanceof PackageElement packageElement
        ) {
            Builders.writeBuilder(
                packageElement,
                typeElement,
                file(typeElement, "Builder"),
                roots,
                enums
            );
            Callbacks.writeCallbacks(
                packageElement,
                typeElement,
                file(typeElement, "Callbacks"),
                roots,
                enums
            );
        }
    }

    private JavaFileObject file(TypeElement te, String type) {
        try {
            return processingEnv.getFiler().createSourceFile(te.getQualifiedName() + type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final String JS_REC = JsonRecord.class.getName();

    private static boolean isJsRec(Set<? extends TypeElement> typedElements) {
        return typedElements != null && typedElements.stream()
            .anyMatch(JsRecProcessor::isJsRec);
    }

    private static boolean isJsRec(TypeElement typeElement) {
        return typeElement.getQualifiedName().toString().equals(JS_REC);
    }

    private static boolean kind(Element e, ElementKind... kinds) {
        return Arrays.asList(kinds).contains(e.getKind());
    }

}
