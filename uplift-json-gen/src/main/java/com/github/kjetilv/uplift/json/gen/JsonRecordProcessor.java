package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;
import com.github.kjetilv.uplift.json.anno.JsonRecord;

import static com.github.kjetilv.uplift.json.gen.GenUtils.packageOf;

@SupportedAnnotationTypes("com.github.kjetilv.uplift.json.anno.*")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public final class JsonRecordProcessor extends AbstractProcessor {

    private TypeMirror jsonRecordType;

    private GenUtils genUtils;

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        var elementUtils = this.processingEnv.getElementUtils();
        this.genUtils = new GenUtils(
            this.processingEnv.getTypeUtils(),
            elementUtils
        );
        this.jsonRecordType = genUtils.lookup(JsonRecord.class).asType();
    }

    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment roundEnv) {
        if (!isInitialized()) {
            throw new IllegalStateException(this + " not initialized");
        }
        if (!containsJsonRecord(typeElements)) {
            return false;
        }
        var types = jsonRecords(roundEnv);
        if (!types.isEmpty()) {
            if (rootless(types)) {
                throw new IllegalStateException("None of " + types.size() + " elements are roots: " + print(types));
            }
            write(types);
        }
        return true;
    }

    private void write(
        Collection<? extends DeclaredType> jsonRecords
    ) {
        var time = time();
        for (var jsonRecord : jsonRecords) {
            if (jsonRecord.asElement() instanceof TypeElement te) {
                var generator = new Generator(
                    te,
                    packageOf(te),
                    time,
                    this::fileForName,
                    genUtils
                );
                try {
                    generator.write();
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to write " + jsonRecord, e);
                }
            } else {
                throw new IllegalStateException("Not a supported type: " + jsonRecord);
            }
        }
    }

    private JavaFileObject fileForName(String name) {
        try {
            return processingEnv.getFiler().createSourceFile(name);
        } catch (Exception e) {
            throw new IllegalStateException("Could not open file " + name, e);
        }
    }

    private boolean containsJsonRecord(Set<? extends TypeElement> typeEls) {
        return Stream.ofNullable(typeEls)
            .flatMap(Collection::stream)
            .map(TypeElement::asType)
            .anyMatch(type ->
                genUtils.isSameType(jsonRecordType, type));
    }

    private boolean rootless(Collection<? extends DeclaredType> els) {
        return els.stream()
            .map(type ->
                type.asElement().getAnnotationsByType(JsonRecord.class))
            .flatMap(Arrays::stream)
            .noneMatch(JsonRecord::root);
    }

    private static String time() {
        return Instant.now().truncatedTo(ChronoUnit.SECONDS)
            .atZone(ZoneId.of("Z"))
            .format(DateTimeFormatter.ISO_DATE_TIME);
    }

    private static boolean isRecord(Element el) {
        return el.getKind() == ElementKind.RECORD;
    }

    private static Collection<DeclaredType> jsonRecords(RoundEnvironment roundEnv) {
        var annotatedRecords = roundEnv.getRootElements()
            .stream()
            .filter(el ->
                el.getAnnotation(JsonRecord.class) != null)
            .peek(JsonRecordProcessor::shouldBeRecord);
        return typeStream(annotatedRecords)
            .map(Element::asType)
            .map(DeclaredType.class::cast)
            .toList();
    }

    private static Stream<? extends Element> typeStream(Stream<? extends Element> els) {
        return els.flatMap(el -> {
            var enclosed = el.getEnclosedElements()
                .stream()
                .filter(JsonRecordProcessor::isRecord);
            return Stream.concat(Stream.of(el), typeStream(enclosed));
        });
    }

    private static void shouldBeRecord(Element el) {
        if (el.getKind() != ElementKind.RECORD) {
            throw new IllegalStateException(
                "Element " + el + " must be of kind " + ElementKind.RECORD + ", was " + el.getKind());
        }
    }

    private static <E extends TypeMirror> String print(Collection<E> roots) {
        return "\n " + roots.stream()
            .map(TypeMirror::toString)
            .map(Objects::toString)
            .collect(Collectors.joining(",\n "));
    }
}
