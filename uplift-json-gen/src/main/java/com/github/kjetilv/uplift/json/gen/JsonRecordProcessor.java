package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.json.anno.JsonRecord;

import static com.github.kjetilv.uplift.json.gen.GenUtils.fqName;

@SupportedAnnotationTypes("com.github.kjetilv.uplift.json.anno.*")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public final class JsonRecordProcessor extends AbstractProcessor {

    private Types typeUtils;

    private Elements elementUtils;

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = this.processingEnv.getTypeUtils();
        elementUtils = this.processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment roundEnv) {
        if (!isInitialized()) {
            throw new IllegalStateException(this + " not initialized");
        }
        if (containsJsonRecord(typeElements)) {
            var typeEls = typeEls(roundEnv);
            var enumEls = enumEls(roundEnv);
            if (typeEls.isEmpty()) {
                if (enumEls.isEmpty()) {
                    return true;
                }
                throw new IllegalStateException("No types for " + enumEls.size() + " enums: " + print(enumEls));
            }
            var notRecords = typeEls.stream()
                .filter(JsonRecordProcessor::notRecord)
                .toList();
            if (notRecords.isEmpty()) {
                if (rootless(typeEls)) {
                    throw new IllegalStateException("None of " + typeEls.size() + " elements are roots: " + print(
                        typeEls));
                }
                write(typeEls, enumEls);
            } else {
                throw new IllegalStateException("Only top-level record types are supported, found " + print(notRecords));
            }
        }
        return false;
    }

    private void write(Collection<? extends Element> typeEls, Collection<? extends Element> enumEls) {
        var time = time();
        for (var el : typeEls) {
            if (el instanceof TypeElement te) {
                var generator = generator(te, typeEls, enumEls, time);
                try {
                    generate(generator);
                    try {
                        var obj = generator.jsonSchema();
                        IO.println(Json.instance().write(obj));
                    } catch (Exception e) {
                        IO.println(fqName(generator.te()) + " could not be paresed");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to write " + el, e);
                }
            } else {
                throw new IllegalStateException("Not a supported type: " + el);
            }
        }
    }

    private Generator generator(
        TypeElement te,
        Collection<? extends Element> types,
        Collection<? extends Element> enums,
        String time
    ) {
        return new Generator(
            GenUtils.packageEl(te),
            te,
            types,
            enums,
            time,
            this::file,
            elementUtils,
            typeUtils
        );
    }

    private JavaFileObject file(String name) {
        try {
            return processingEnv.getFiler().createSourceFile(name);
        } catch (Exception e) {
            throw new IllegalStateException("Could not open file " + name, e);
        }
    }

    private boolean containsJsonRecord(Set<? extends TypeElement> typeEls) {
        return Stream.ofNullable(typeEls)
            .flatMap(Collection::stream)
            .anyMatch(this::isJsonRecord);
    }

    private boolean rootless(Collection<? extends Element> els) {
        return els.stream()
            .filter(TypeElement.class::isInstance)
            .map(element ->
                element.getAnnotation(JsonRecord.class))
            .filter(Objects::nonNull)
            .noneMatch(JsonRecord::root);
    }

    private boolean isJsonRecord(TypeElement typeEl) {
        return typeEl.getQualifiedName().toString().equals(JsonRecord.class.getName());
    }

    private static void generate(Generator generator) {
        if (generator.isRoot()) {
            generator.writeRW();
        }
        generator.writeBuilder();
        generator.writeCallbacks();
        generator.writeWriter();
    }

    private static String time() {
        return Instant.now().truncatedTo(ChronoUnit.SECONDS)
            .atZone(ZoneId.of("Z"))
            .format(DateTimeFormatter.ISO_DATE_TIME);
    }

    private static boolean notRecord(Element element) {
        return !(element.getKind() == ElementKind.RECORD &&
                 element instanceof TypeElement typeEl &&
                 isPackageOrClass(typeEl.getEnclosingElement()));
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
        return "\n " + roots.stream()
            .map(Element::asType)
            .map(TypeMirror::toString)
            .map(Objects::toString)
            .collect(Collectors.joining(",\n "));
    }
}
