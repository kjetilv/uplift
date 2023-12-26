package com.github.kjetilv.uplift.json.annpro;

import com.github.kjetilv.uplift.json.anno.JsRec;

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

import static com.github.kjetilv.uplift.json.annpro.Gen.*;

@SupportedAnnotationTypes("com.github.kjetilv.uplift.json.anno.*")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class JsRecProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> typedElements, RoundEnvironment roundEnv) {
        return typedElements.stream()
            .filter(typeElement ->
                typeElement.getQualifiedName().toString().equals(JsRec.class.getName()))
            .findFirst()
            .map(typeElement -> {
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
            ).orElse(false);
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

    private static boolean kind(Element e, ElementKind... kinds) {
        return Arrays.asList(kinds).contains(e.getKind());
    }

}
