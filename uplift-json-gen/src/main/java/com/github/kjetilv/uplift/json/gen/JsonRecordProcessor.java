package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.json.anno.JsonRecord;

@SupportedAnnotationTypes("com.github.kjetilv.uplift.json.anno.*")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public final class JsonRecordProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment roundEnv) {
        if (containsJsonRecord(typeElements)) {
            var typeEls = typeEls(roundEnv);
            var enumEls = enumEls(roundEnv);
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

    private void write(Collection<? extends Element> typeEls, Collection<? extends Element> enumEls) {
        var time = time();
        for (var el : typeEls) {
            if (el instanceof TypeElement te) {
                var pe = GenUtils.packageEl(te);
                var generator = new Generator(pe, te, time, this::file);
                try {
                    if (generator.isRoot()) {
                        generator.writeRW();
                    }
                    generator.writeBuilder(typeEls, enumEls);
                    generator.writeCallbacks(typeEls, enumEls, generator.isRoot());
                    generator.writeWriter(typeEls, enumEls);
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to write " + el, e);
                }
                try {
                    IO.println(generator.jsonSchema(te));
                } catch (Exception e) {
                    IO.println(GenUtils.fqName(te) + " could not be paresed");
                    e.printStackTrace();
                }
            } else {
                throw new IllegalStateException("Not a supported type: " + el);
            }
        }
    }

    private JavaFileObject file(String name) {
        try {
            return processingEnv.getFiler().createSourceFile(name);
        } catch (Exception e) {
            throw new IllegalStateException("Could not open file " + name, e);
        }
    }

    private static String time() {
        return Instant.now().truncatedTo(ChronoUnit.SECONDS)
            .atZone(ZoneId.of("Z"))
            .format(DateTimeFormatter.ISO_DATE_TIME);
    }

    private static void requireAllRecords(Collection<? extends Element> els) {
        if (els.stream().anyMatch(JsonRecordProcessor::isNotRecord)) {
            var found = print(els, JsonRecordProcessor::isNotRecord);
            throw new IllegalStateException("Only top-level record types are supported, found " + found);
        }
    }

    private static void requireEmptyEnums(Collection<? extends Element> els) {
        if (!els.isEmpty()) {
            throw new IllegalStateException("No types for " + els.size() + " enums: " + print(els));
        }
    }

    private static void requireAnyRootType(Collection<? extends Element> els) {
        if (rootless(els)) {
            throw new IllegalStateException("None of " + els.size() + " elements are roots: " + print(els));
        }
    }

    private static boolean rootless(Collection<? extends Element> els) {
        return els.stream()
            .filter(TypeElement.class::isInstance)
            .map(element ->
                element.getAnnotation(JsonRecord.class))
            .filter(Objects::nonNull)
            .noneMatch(JsonRecord::root);
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
        return el.getKind() == ElementKind.RECORD &&
               el instanceof TypeElement typeEl &&
               isPackageOrClass(typeEl.getEnclosingElement());
    }

    private static boolean isPackageOrClass(Element enclosingEl) {
        return enclosingEl instanceof PackageElement || enclosingEl instanceof TypeElement;
    }

    private static Collection<? extends Element> enumEls(RoundEnvironment roundEnv) {
        return GenUtils.enums(roundEnv.getRootElements())
            .collect(Collectors.toSet());
    }

    private static Collection<? extends Element> typeEls(RoundEnvironment roundEnv) {
        var annotatedRecords = roundEnv.getRootElements()
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

    private static <E extends Element> String print(Collection<E> roots) {
        return print(roots, null);
    }

    private static <E extends Element> String print(Collection<E> roots, Predicate<E> filter) {
        return "\n " + roots.stream()
            .filter(filter == null ? _ -> true : filter)
            .map(Element::asType)
            .map(TypeMirror::toString)
            .map(Objects::toString)
            .collect(Collectors.joining(",\n "));
    }
}
