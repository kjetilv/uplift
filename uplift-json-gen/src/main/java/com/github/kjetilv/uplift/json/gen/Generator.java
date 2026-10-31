package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.FieldEvents;
import com.github.kjetilv.uplift.json.MapWriter;
import com.github.kjetilv.uplift.json.ObjectWriter;
import com.github.kjetilv.uplift.json.anno.JsonRecord;
import com.github.kjetilv.uplift.json.callbacks.MapCallbacks;

import javax.lang.model.type.TypeKind;

import static com.github.kjetilv.uplift.json.gen.Names.*;
import static com.github.kjetilv.uplift.json.gen.SourceTypes.render;

/// Generates the sources for one record type. All types in generated code are fully qualified, so
/// generated sources need no imports, and cannot conflict with user types.
final class Generator {

    private final TypeElement jsonRecord;

    private final PackageElement jsonRecordPackage;

    private final Provenance provenance;

    private final Function<String, JavaFileObject> fileForName;

    private final GenUtils utils;

    private final List<RecordAttribute> attributes;

    private final String recordName;

    Generator(
        TypeElement jsonRecord,
        PackageElement jsonRecordPackage,
        Provenance provenance,
        Function<String, JavaFileObject> fileForName,
        GenUtils utils
    ) {
        this.jsonRecord = Objects.requireNonNull(jsonRecord, "jsonRecord");
        this.jsonRecordPackage = Objects.requireNonNull(jsonRecordPackage, "jsonRecordPackage");
        this.provenance = provenance;
        this.fileForName = Objects.requireNonNull(fileForName, "fileForName");
        this.utils = Objects.requireNonNull(utils, "utils");
        this.attributes = jsonRecord.getRecordComponents()
            .stream()
            .map(utils::attribute)
            .toList();
        this.recordName = relativeName(jsonRecord);
    }

    void write() {
        if (isRoot()) {
            write(factoryClass(), rwSource());
        }
        write(writerClass(), writerSource());
        write(builderClass(), builderSource());
        write(callbacksClass(), callbacksSource());
    }

    private String rwSource() {
        return header("Reading and writing") +
               """
                   public final class %1$s implements %2$s<%3$s> {
                   
                       public static final %2$s<%3$s> INSTANCE = new %1$s();
                   
                       @Override
                       public %4$s<%5$s<%3$s>, %6$s> callbacks() {
                           return %7$s::create;
                       }
                   
                       @Override
                       public %6$s callbacks(%5$s<%3$s> onDone) {
                           return %7$s.create(onDone);
                       }
                   
                       @Override
                       public %8$s<%3$s> objectWriter() {
                           return new %9$s();
                       }
                   
                       private %1$s() {
                       }
                   }
                   """.formatted(
                   factoryClass(),
                   name(JsonRW.class),
                   recordName,
                   name(Function.class),
                   name(Consumer.class),
                   name(Callbacks.class),
                   callbacksClass(),
                   name(ObjectWriter.class),
                   writerClass()
               );
    }

    private String writerSource() {
        var calls = attributes.stream()
            .map(attribute ->
                "\n            ." + writeCall(attribute))
            .collect(Collectors.joining());
        return header("Writer for") +
               """
                   final class %1$s extends %2$s<%3$s> {
                   
                       @Override
                       protected %4$s doWrite(%3$s object, %4$s events) {
                           return events%5$s;
                       }
                   }
                   """.formatted(
                   writerClass(),
                   name(AbstractObjectWriter.class),
                   recordName,
                   name(FieldEvents.class),
                   calls
               );
    }

    private String builderSource() {
        var members = attributes.stream()
            .map(this::builderMembers)
            .collect(Collectors.joining());
        var arguments = attributes.stream()
            .map(attribute ->
                "            " + constructorArgument(attribute))
            .collect(Collectors.joining(",\n"));
        var converters = attributes.stream()
            .filter(RecordAttribute::isPrimitiveArray)
            .map(attribute ->
                attribute.componentType().getKind())
            .distinct()
            .map(this::converter)
            .collect(Collectors.joining());
        return header("Builder for") +
               """
                   final class %1$s implements %2$s<%3$s> {
                   
                       static %1$s create() {
                           return new %1$s();
                       }
                   
                       private %1$s() {
                       }
                   %4$s
                       @Override
                       public %3$s get() {
                           return new %3$s(
                   %5$s
                           );
                       }
                   %6$s}
                   """.formatted(
                   builderClass(),
                   name(Supplier.class),
                   recordName,
                   members,
                   arguments,
                   converters
               );
    }

    private String callbacksSource() {
        var registrations = attributes.stream()
            .map(attribute ->
                "        PRESETS." + callbackRegistration(attribute) + ";");
        var subs = attributes.stream()
            .filter(RecordAttribute::isRecord)
            .map(attribute ->
                generatedClass(attribute.componentType(), "_Callbacks"))
            .distinct()
            .map(callbacksClass ->
                "        PRESETS.sub(" + callbacksClass + ".PRESETS);");
        var buildTokens = Stream.of("        PRESETS.buildTokens();")
            .filter(_ -> isRoot());
        var initializers = Stream.of(registrations, subs, buildTokens)
            .flatMap(Function.identity())
            .collect(Collectors.joining("\n"));
        return header("Callbacks for") +
               """
                   final class %1$s {
                   
                       static %2$s<%3$s, %4$s> create(%5$s<%4$s> onDone) {
                           return create(null, onDone);
                       }
                   
                       static %2$s<%3$s, %4$s> create(%6$s parent, %5$s<%4$s> onDone) {
                           return new %2$s<>(
                               %3$s.create(),
                               parent,
                               PRESETS.getNumbers(),
                               PRESETS.getStrings(),
                               PRESETS.getBooleans(),
                               PRESETS.getObjects(),
                               onDone,
                               PRESETS.getTokenTable()
                           );
                       }
                   
                       static final %7$s<%3$s, %4$s> PRESETS;
                   
                       static {
                           PRESETS = new %7$s<>(%8$s.class);
                   %9$s
                       }
                   }
                   """.formatted(
                   callbacksClass(),
                   name(PresetCallbacks.class),
                   builderClass(),
                   recordName,
                   name(Consumer.class),
                   name(Callbacks.class),
                   name(PresetCallbacksInitializer.class),
                   jsonRecord.getQualifiedName(),
                   initializers
               );
    }

    private String header(String description) {
        var packageDeclaration = jsonRecordPackage.isUnnamed()
            ? ""
            : "package " + jsonRecordPackage.getQualifiedName() + ";\n\n";
        var generatedBy = provenance == null
            ? "Generated using uplift"
            : "Generated at " + provenance.timestamp() + " by " + provenance.user() + " using uplift";
        var date = provenance == null
            ? ""
            : "\n    date = " + quoted(provenance.timestamp()) + ",";
        return packageDeclaration +
               """
                   /// %1$s [%2$s]
                   ///
                   /// %3$s
                   @%4$s(
                       value = %5$s,%6$s
                       comments = %7$s
                   )
                   """.formatted(
                   description,
                   recordName,
                   generatedBy,
                   name(Generated.class),
                   quoted(name(JsonRecordProcessor.class)),
                   date,
                   quoted(description + " " + recordName)
               );
    }

    private String callbackRegistration(RecordAttribute attribute) {
        var jsonName = quoted(attribute.jsonName());
        return switch (attribute.kind()) {
            case VALUE -> "on%s(%s, %s::%s)".formatted(
                attribute.callbackEvent(),
                jsonName,
                builderClass(),
                attribute.mutator()
            );
            case ENUM -> "onEnum(%s, %s::valueOf, %s::%s)".formatted(
                jsonName,
                render(attribute.componentType()),
                builderClass(),
                attribute.mutator()
            );
            case RECORD -> "onObject(%s, (callbacks, builder) -> %s.create(callbacks, builder::%s))".formatted(
                jsonName,
                generatedClass(attribute.componentType(), "_Callbacks"),
                attribute.mutator()
            );
            case MAP -> "onObject(%s, (callbacks, builder) -> %s.create(callbacks, builder::%s))".formatted(
                jsonName,
                name(MapCallbacks.class),
                attribute.mutator()
            );
        };
    }

    private String writeCall(RecordAttribute attribute) {
        var writer = switch (attribute.kind()) {
            case MAP -> ", new " + name(MapWriter.class) + "()";
            case RECORD -> ", new " + generatedClass(attribute.componentType(), "_Writer") + "()";
            case VALUE, ENUM -> attribute.requiresConversion() ? ", this::value" : "";
        };
        return "%s(%s, object.%s()%s)".formatted(
            attribute.fieldEvent(),
            quoted(attribute.jsonName()),
            attribute.javaName(),
            writer
        );
    }

    private String builderMembers(RecordAttribute attribute) {
        return switch (attribute.cardinality()) {
            case SINGLE -> """
                
                    private %1$s %2$s;
                
                    void %3$s(%1$s %2$s) {
                        this.%2$s = %2$s;
                    }
                """.formatted(
                render(attribute.element().asType()),
                attribute.javaName(),
                attribute.setter()
            );
            case LIST, ARRAY -> """
                
                    private %1$s<%2$s> %3$s;
                
                    void %4$s(%5$s %6$s) {
                        if (this.%3$s == null) {
                            this.%3$s = new %7$s<>();
                        }
                        this.%3$s.add(%6$s);
                    }
                """.formatted(
                name(List.class),
                utils.boxedName(attribute.componentType()),
                attribute.javaName(),
                attribute.adder(),
                render(attribute.componentType()),
                attribute.singularName(),
                name(ArrayList.class)
            );
        };
    }

    private String constructorArgument(RecordAttribute attribute) {
        var name = attribute.javaName();
        if (attribute.cardinality() != RecordAttribute.Cardinality.ARRAY) {
            return name;
        }
        var conversion = attribute.isPrimitiveArray()
            ? converterName(attribute.componentType().getKind()) + "(" + name + ")"
            : name + ".toArray(" + render(attribute.componentType()) + "[]::new)";
        return name + " == null ? null : " + conversion;
    }

    private String converter(TypeKind primitiveKind) {
        return """
            
                private static %1$s[] %2$s(%3$s<%4$s> list) {
                    var result = new %1$s[list.size()];
                    var i = 0;
                    for (%4$s value : list) {
                        result[i++] = value;
                    }
                    return result;
                }
            """.formatted(
            primitiveName(primitiveKind),
            converterName(primitiveKind),
            name(List.class),
            utils.boxedName(primitiveKind)
        );
    }

    private boolean isRoot() {
        var annotation = jsonRecord.getAnnotation(JsonRecord.class);
        return annotation != null && annotation.root();
    }

    private void write(String className, String source) {
        var name = qualified(jsonRecordPackage, className);
        try (var writer = fileForName.apply(name).openWriter()) {
            writer.write(source);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write " + name, e);
        }
    }

    private String factoryClass() {
        var annotation = jsonRecord.getAnnotation(JsonRecord.class);
        return annotation == null || annotation.factoryClass().isBlank()
            ? jsonRecord.getSimpleName() + DEFAULT_FACTORY_SUFFIX
            : annotation.factoryClass();
    }

    private String writerClass() {
        return flatName(jsonRecord) + "_Writer";
    }

    private String builderClass() {
        return flatName(jsonRecord) + "_Builder";
    }

    private String callbacksClass() {
        return flatName(jsonRecord) + "_Callbacks";
    }

    /// @param recordType Another record type
    /// @param suffix Suffix of the generated class
    /// @return Qualified name of a class generated for the other record type
    private String generatedClass(TypeMirror recordType, String suffix) {
        var typeElement = utils.typeElement(recordType);
        return qualified(utils.packageOf(typeElement), flatName(typeElement) + suffix);
    }

    private static final String DEFAULT_FACTORY_SUFFIX = "RW";

    private static String name(Class<?> type) {
        return type.getCanonicalName();
    }

    private static String primitiveName(TypeKind primitiveKind) {
        return primitiveKind.name().toLowerCase(Locale.ROOT);
    }

    private static String converterName(TypeKind primitiveKind) {
        return "to" + upcased(primitiveName(primitiveKind)) + "Array";
    }

    /// Origin of generated code, written to the generated sources
    ///
    /// @param timestamp Time of generation
    /// @param user Name of user
    record Provenance(String timestamp, String user) {
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + jsonRecord.getQualifiedName() + "]";
    }
}
