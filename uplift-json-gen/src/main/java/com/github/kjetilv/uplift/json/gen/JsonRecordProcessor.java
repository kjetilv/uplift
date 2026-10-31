package com.github.kjetilv.uplift.json.gen;

import module java.base;
import com.github.kjetilv.uplift.json.anno.JsonRecord;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

// Class imports, because maven-compiler-plugin 3.14.0 fails on module imports

/// Generates readers and writers for records annotated with [JsonRecord], and for records nested in them.
@SupportedAnnotationTypes("com.github.kjetilv.uplift.json.anno.*")
@SupportedOptions(JsonRecordProcessor.REPRODUCIBLE)
public final class JsonRecordProcessor extends AbstractProcessor {

    /// Processor option. If `true`, generated sources carry no timestamp and no user name.
    public static final String REPRODUCIBLE = "uplift.json.gen.reproducible";

    private GenUtils genUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.genUtils = new GenUtils(
            processingEnv.getTypeUtils(),
            processingEnv.getElementUtils()
        );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        var annotated = roundEnv.getElementsAnnotatedWith(JsonRecord.class);
        if (annotated.isEmpty()) {
            return false;
        }
        var nonRecords = annotated.stream()
            .filter(element ->
                element.getKind() != ElementKind.RECORD)
            .toList();
        if (!nonRecords.isEmpty()) {
            nonRecords.forEach(element ->
                error(
                    "@" + JsonRecord.class.getSimpleName() + " requires a record, " +
                    element.getSimpleName() + " is of kind " + element.getKind(),
                    element
                ));
            return true;
        }
        if (annotated.stream().noneMatch(JsonRecordProcessor::isRoot)) {
            annotated.forEach(element ->
                error(
                    "None of " + annotated.size() + " @" + JsonRecord.class.getSimpleName() + " types are roots",
                    element
                ));
            return true;
        }
        var provenance = provenance();
        annotated.stream()
            .map(TypeElement.class::cast)
            .flatMap(JsonRecordProcessor::withNestedRecords)
            .distinct()
            .forEach(jsonRecord ->
                generate(jsonRecord, provenance.orElse(null)));
        return true;
    }

    private void generate(TypeElement jsonRecord, Generator.Provenance provenance) {
        try {
            new Generator(
                jsonRecord,
                genUtils.packageOf(jsonRecord),
                provenance,
                name -> sourceFile(name, jsonRecord),
                genUtils
            ).write();
        } catch (GenException e) {
            error(e.getMessage(), e.element());
        } catch (Exception e) {
            error("Failed to generate code for " + jsonRecord.getQualifiedName() + ": " + e, jsonRecord);
        }
    }

    private JavaFileObject sourceFile(String name, Element origin) {
        try {
            return processingEnv.getFiler().createSourceFile(name, origin);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create source file " + name, e);
        }
    }

    private void error(String message, Element element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private Optional<Generator.Provenance> provenance() {
        return Boolean.parseBoolean(processingEnv.getOptions().get(REPRODUCIBLE))
            ? Optional.empty()
            : Optional.of(new Generator.Provenance(time(), System.getProperty("user.name")));
    }

    private static boolean isRoot(Element element) {
        var annotation = element.getAnnotation(JsonRecord.class);
        return annotation != null && annotation.root();
    }

    private static Stream<TypeElement> withNestedRecords(TypeElement typeElement) {
        return Stream.concat(
            Stream.of(typeElement),
            typeElement.getEnclosedElements()
                .stream()
                .filter(element ->
                    element.getKind() == ElementKind.RECORD)
                .map(TypeElement.class::cast)
                .flatMap(JsonRecordProcessor::withNestedRecords)
        );
    }

    private static String time() {
        return Instant.now().truncatedTo(ChronoUnit.SECONDS)
            .atZone(ZoneId.of("Z"))
            .format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
