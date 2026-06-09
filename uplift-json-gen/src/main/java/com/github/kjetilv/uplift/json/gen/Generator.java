package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;
import com.github.kjetilv.uplift.json.*;
import com.github.kjetilv.uplift.json.anno.JsonRecord;

import static com.github.kjetilv.uplift.json.gen.GenUtils.*;

final class Generator {

    private final PackageElement jsonRecordPackage;

    private final TypeElement jsonRecord;

    private final Collection<? extends DeclaredType> jsonRecords;

    private final Collection<? extends DeclaredType> enums;

    private final String timestamp;

    private final Function<String, JavaFileObject> filer;

    private final Elements elementUtils;

    private final Types typeUtils;

    private final GenUtils utils;

    private final List<RecordAttribute> recordAttributes;

    Generator(
        PackageElement jsonRecordPackage,
        TypeElement jsonRecord,
        Collection<? extends DeclaredType> jsonRecords,
        Collection<? extends DeclaredType> enums,
        String timestamp,
        Function<String, JavaFileObject> filer,
        Elements elementUtils,
        Types typeUtils
    ) {
        this.jsonRecordPackage = jsonRecordPackage;
        this.jsonRecord = jsonRecord;

        this.jsonRecords = jsonRecords;
        this.enums = enums;

        this.timestamp = timestamp;
        this.filer = filer;
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
        this.utils = new GenUtils(typeUtils, elementUtils);
        this.recordAttributes = this.recordAttributes(jsonRecord);
    }

    public void write() {
        if (isRoot()) {
            writeRW();
        }
        writeBuilder();
        writeCallbacks();
        writeWriter();
        try {
            var obj = jsonSchema();
            IO.println(Json.instance().write(obj));
        } catch (Exception e) {
            IO.println(fqName(jsonRecord) + " could not be parsed");
            e.printStackTrace();
        }
    }

    void writeCallbacks() {
        var unqualifiedName = unqTypeName();
        try (var bw = writer(callbackFile())) {
            if (!jsonRecordPackage.isUnnamed()) {
                write(
                    bw,
                    "package " + jsonRecordPackage.getQualifiedName() + ";",
                    ""
                );
            }
            write(
                bw,
                importType(Consumer.class),
                importType(Generated.class),
                "",
                importType(Callbacks.class),
                importType(JsonRecordProcessor.class),
                importType(PresetCallbacks.class),
                importType(PresetCallbacksInitializer.class),
                "",
                "/// Callbacks for [" + unqualifiedName + "]",
                "///",
                "/// Generated at " + timestamp + " by " + System.getProperty("user.name") + " using uplift",
                "@" + GENERATED + "(",
                "    value = \"" + JsonRecordProcessor.class.getName() + "\",",
                "    date = \"" + timestamp + "\",",
                "    comments = \"Callbacks for " + unqualifiedName + "\"",
                ")",
                "final class " + callbacksClassPlain(jsonRecord) + " {",
                "",
                "    static " + PRESET_CALLBACKS + "<" + builderClassPlain(jsonRecord) + ", " + unqualifiedName + "> create(",
                "        " + CONSUMER + "<" + unqualifiedName + "> onDone",
                "    ) {",
                "        return create(null, onDone);",
                "    }",
                "",
                "    static " + PRESET_CALLBACKS + "<" + builderClassPlain(jsonRecord) + ", " + unqualifiedName + "> create(",
                "        " + CALLBACKS + " parent, ",
                "        " + CONSUMER + "<" + unqualifiedName + "> onDone",
                "    ) {",
                "        return new " + PRESET_CALLBACKS + "<>(",
                "            " + builderClassPlain(jsonRecord) + ".create(),",
                "            parent,",
                "            PRESETS.getNumbers(),",
                "            PRESETS.getStrings(),",
                "            PRESETS.getBooleans(),",
                "            PRESETS.getObjects(),",
                "            onDone,",
                "            PRESETS.getTokenTrie()",
                "        );",
                "    }",
                "",
                "    static final " + PRESET_CALLBACKS_INITIALIZER + "<" + builderClassPlain(jsonRecord) + ", " + unqualifiedName + "> PRESETS;"
            );
            write(bw, "");
            write(bw, "    static {");
            write(bw, "        PRESETS = new " + PRESET_CALLBACKS_INITIALIZER + "<>(");
            write(bw, "            " + jsonRecord.getQualifiedName() + ".class");
            write(bw, "        );");
            write(
                bw,
                recordAttributes(jsonRecord)
                    .stream()
                    .map(recordAttribute ->
                        recordAttribute.callbackHandler(jsonRecord))
                    .map(event ->
                        "        PRESETS." + event + ";"
                    )
                    .toList()
            );

            write(
                bw,
                recordAttributes.stream()
                    .filter(RecordAttribute::isGenerated)
                    .map(attribute ->
                        "        PRESETS.sub(" + callbacksClassPlain(attribute.internalType()) + ".PRESETS);")
                    .toList()
            );

            if (isRoot()) {
                write(bw, "        PRESETS.buildTokens();");
            }
            write(bw, "    }", "}");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write callbacks for " + jsonRecord, e);
        }
    }

    boolean isRoot() {
        return Optional.ofNullable(jsonRecord.getAnnotation(JsonRecord.class))
            .map(JsonRecord::root)
            .orElse(false);
    }

    void writeRW() {
        var unqualifiedName = unqTypeName();
        var file = factoryFile(jsonRecordPackage, jsonRecord);
        try (var bw = writer(file)) {
            if (!jsonRecordPackage.isUnnamed()) {
                write(
                    bw,
                    "package " + jsonRecordPackage.getQualifiedName() + ";",
                    ""
                );
            }
            write(
                bw,
                importType(Consumer.class),
                importType(Function.class),
                importType(Generated.class),
                "",
                importType(Callbacks.class),
                importType(ObjectWriter.class),
                importType(JsonRW.class),
                "",
                "/// Reading and writing instances of [" + unqualifiedName + "]",
                "///",
                "/// Generated @ " + timestamp + " by " + System.getProperty("user.name") + " using uplift",
                "@" + GENERATED + "(",
                "    value = \"" + JsonRecordProcessor.class.getName() + "\",",
                "    date = \"" + timestamp + "\",",
                "    comments = \"Reading and writing " + unqualifiedName + "\"",
                ")",
                "public final class " + factoryClass(jsonRecord) + " implements " + JSON_RW + "<" + unqualifiedName + "> {",
                "",
                "    public static " + JSON_RW + "<" + unqualifiedName + "> INSTANCE = new " + factoryClass(jsonRecord) + "();",
                "",
                "    @Override",
                "    public " + FUNCTION + "<" + CONSUMER + "<" + unqualifiedName + ">, " + CALLBACKS + "> callbacks() {",
                "        return " + callbacksClassPlain(jsonRecord) + "::create;",
                "    }",
                "",
                "    @Override",
                "    public " + CALLBACKS + " callbacks(" + CONSUMER + "<" + unqualifiedName + "> onDone) {",
                "        return " + callbacksClassPlain(jsonRecord) + ".create(onDone);",
                "    }",
                "",
                "    @Override",
                "    public " + OBJECT_WRITER + "<" + unqualifiedName + "> objectWriter() {",
                "        return new " + writerClassPlain(jsonRecord) + "();",
                "    }",
                "",
                "    private " + factoryClass(jsonRecord) + "() {",
                "    }",
                "}"
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write factory for " + jsonRecord, e);
        }

    }

    Map<String, ?> jsonSchema() {
        Map<String, ?> type = jsonRecord.getRecordComponents()
            .stream()
            .collect(
                Collectors.toMap(
                    GenUtils::fieldName,
                    this::jsonType,
                    (e1, e2) -> {
                        throw new IllegalStateException("Duplicate field name: " + e1 + "/" + e2);
                    },
                    LinkedHashMap::new
                ));
        return Map.ofEntries(
            Map.entry("$schema", "https://json-schema.org/draft/2020-12/schema"),
            Map.entry("description", unqTypeName()),
            Map.entry("type", "object"),
            Map.entry("properties", type)
        );
    }

    void writeWriter() {
        var file = writerFile(jsonRecord);
        try (var bw = writer(file)) {
            var unqualifiedName = unqTypeName();
            if (!jsonRecordPackage.isUnnamed()) {
                write(
                    bw,
                    "package " + jsonRecordPackage.getQualifiedName() + ";",
                    ""
                );
            }
            write(
                bw,
                importType(Generated.class),
                "",
                importType(AbstractObjectWriter.class),
                importType(FieldEvents.class),
                "",
                "/// Writer for [" + unqualifiedName + "]",
                "///",
                "/// Generated at " + timestamp + " by " + System.getProperty("user.name") + " using uplift",
                "@" + GENERATED + "(",
                "    value = \"" + JsonRecordProcessor.class.getName() + "\",",
                "    date = \"" + timestamp + "\",",
                "    comments = \"Writer for " + unqualifiedName + "\"",
                ")",
                "final class " + writerClassPlain(jsonRecord),
                "    extends " + ABSTRACT_OBJECT_WRITER + "<" + unqualifiedName + ">",
                "{",
                "    protected " + FIELD_EVENTS + " doWrite(",
                "        " + unqualifiedName + " " + variableName(jsonRecord) + ", ",
                "        " + FIELD_EVENTS + " events",
                "    ) {",
                "        return events"
            );
            recordAttributes.forEach(attr ->
                write(
                    bw,
                    "            ." + writeCall(attr, jsonRecord)
                ));
            write(
                bw,
                "        ;",
                "    }",
                "}"
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write callbacks for " + jsonRecord, e);
        }
    }

    void writeBuilder() {
        var file = builderFile(jsonRecord);
        var setters = jsonRecord.getRecordComponents()
            .stream().flatMap(el -> {
                var type = el.asType();
                return Stream.of(
                    "    private " + print(type) + " " + fieldName(el) + ";",
                    "",
                    "    void " + setter(el) + "(" + print(type) + " " + fieldName(el) + ") {",
                    "        this." + fieldName(el) + " = " + fieldName(el) + ";",
                    "    }",
                    ""
                );
            })
            .toList();

        var adders = jsonRecord.getRecordComponents()
            .stream()
            .flatMap(element ->
                utils.iterableType(element, jsonRecords, enums)
                    .stream()
                    .flatMap(listType ->
                        Stream.of(
                            "    void " + adder(element) + "(" + listType + " " + singularVariableName(element) + ") {",
                            "        if (this." + fieldName(element) + " == null) {",
                            "            this." + fieldName(element) + " = new " + ArrayList.class.getName() + "();",
                            "        }",
                            "        this." + fieldName(element) + ".add(" + singularVariableName(element) + ");",
                            "    }",
                            ""
                        ))
            )
            .toList();

        var unqualifiedName = unqTypeName();
        var creatorStart = List.of(
            "    @Override",
            "    public " + unqualifiedName + " get() {",
            "        return new " + unqualifiedName + "("
        );

        List<String> creatorMeat = jsonRecord.getRecordComponents()
            .stream()
            .map(el ->
                "            " + fieldName(el) + ",")
            .collect(Collectors.toCollection(LinkedList::new));
        var last = creatorMeat.removeLast();
        creatorMeat.addLast(last.substring(0, last.length() - 1));

        var creatorEnd = List.of(
            "        );",
            "     }"
        );

        try (var bw = writer(file)) {
            if (!jsonRecordPackage.isUnnamed()) {
                write(
                    bw,
                    "package " + jsonRecordPackage.getQualifiedName() + ";",
                    ""
                );
            }
            write(
                bw,
                importType(Supplier.class),
                importType(Generated.class),
                "",
                "/// Builder for [" + unqualifiedName + "]",
                "///",
                "/// Generated at " + timestamp + " by " + System.getProperty("user.name") + " using uplift",
                "@" + GENERATED + "(",
                "    value = \"" + JsonRecordProcessor.class.getName() + "\",",
                "    date = \"" + timestamp + "\",",
                "    comments = \"Builder for " + unqualifiedName + "\"",
                ")",
                "final class " + builderClassPlain(jsonRecord) + " implements " + SUPPLIER + "<" + unqualifiedName + "> {",
                "",
                "    static " + builderClassPlain(jsonRecord) + " create() {",
                "        return new " + builderClassPlain(jsonRecord) + "();",
                "    }",
                "",
                "    private " + builderClassPlain(jsonRecord) + "() {",
                "    }"
            );
            write(bw, "");
            write(bw, setters);
            write(bw, adders);
            write(bw, creatorStart);
            write(bw, creatorMeat);
            write(bw, creatorEnd);
            write(bw, "}");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write builder for " + jsonRecord, e);
        }
    }

    private List<RecordAttribute> recordAttributes(TypeElement jsonRecord) {
        return jsonRecord.getRecordComponents()
            .stream()
            .filter(element ->
                element.getKind() == ElementKind.RECORD_COMPONENT)
            .map(utils::create)
            .toList();
    }

    private String writeCall(RecordAttribute recordAttribute, TypeElement te) {
        var attribute = recordAttribute.attribute();
        Optional<DeclaredType> listType = Optional.empty();//utils.iterableType(attribute);
        var isEnum = utils.fieldType(attribute, enums)
            .or(() -> utils.iterableType(attribute, enums))
            .isPresent();
        var isRoot = utils.fieldType(attribute, jsonRecords)
            .or(() -> utils.iterableType(attribute, jsonRecords))
            .isPresent();
        var isMap = isMap(te);
        var convert = false;
//            !isMap && !isRoot && (isEnum || listType.map(BaseType::of)
//            .orElseGet(() -> BaseType.of(attribute))
//            .requiresConversion());
        var name = isRoot ? "object"
            : isMap ? "map"
                : isEnum ? "string"
                    : recordAttribute.fieldEvent();
//                    : listType.map(BaseType::of).orElseGet(() -> BaseType.of(attribute)).methodName();
//        String u = listType
//            .map((DeclaredType listTypeName) ->
//                writerClass(attribute, listTypeName.asElement().getSimpleName().toString()))
//            .orElseGet(getStringSupplier(attribute));
        return name +
               listType.map(_ -> "Array").orElse("") +
               "(" +
               quote(attribute.getSimpleName()) + ", " + variableName(te) + "." + attribute.getSimpleName() + "()" +
               (convert ? ", this::value)"
//                   : isRoot ? ", new " + u + "())"
                       : isMap ? ", new " + MapWriter.class.getName() + "())"
                           : ")");
    }

    private boolean isMap(TypeElement te) {
        return isMap(te.asType());
    }

    private boolean isMap(TypeMirror type) {
        return typeUtils.isAssignable(type, utils.fetch(Map.class));
    }

    private Supplier<String> getStringSupplier(RecordComponentElement attribute) {
        return () -> writerClass(attribute, attribute.asType().toString());
    }

    private String writerClass(RecordComponentElement attribute, String name) {
        var packageElement = packageOf(attribute);
        if (packageElement.isUnnamed()) {
            return name + "_Writer";
        }
        var prefix = packageElement.toString();
        var suffix = name.substring(prefix.length() + 1);
        return suffix.replace('.', '_') + "_Writer";
    }

    private Map<String, Object> jsonType(RecordComponentElement element) {
        return utils.iterableType(element, jsonRecords, enums)
            .map(listType ->
                Map.of(
                    "type", "array",
                    "items", Map.of(
                        "type", listType
                    )
                ))
            .orElseGet(() ->
                Map.of(
                    "type", jsonTypeSingular(element)
                ));
    }

    private String jsonTypeSingular(RecordComponentElement element) {
//        typeUtils.isAssignable(typeUtils.g).
        return isMap(element.asType()) ? "object"
//            : utils.isMap(element, jsonRecords) || isMap(element) ? "object"
//                : isMap(element, enums) ? "string"
            : utils.iterableType(element).isPresent() ? "array"
                : baseJsonType(element);
    }

    private String unqTypeName() {
        var name = jsonRecord.getQualifiedName();
        var fullName = name.toString();
        if (jsonRecordPackage.isUnnamed()) {
            return fullName;
        }
        var prefix = jsonRecordPackage.getQualifiedName().toString();
        return fullName.startsWith(prefix)
            ? fullName.substring(prefix.length() + 1)
            : fullName;
    }

    private JavaFileObject factoryFile(PackageElement packageEl, TypeElement typeEl) {
        return file(factoryClassQ(packageEl, typeEl));
    }

    private JavaFileObject callbackFile() {
        return classFileName(jsonRecord, "Callbacks");
    }

    private JavaFileObject writerFile(TypeElement typeEl) {
        return classFileName(typeEl, "Writer");
    }

    private JavaFileObject builderFile(TypeElement typeEl) {
        return classFileName(typeEl, "Builder");
    }

    private JavaFileObject classFileName(TypeElement typeEl, String callbacks) {
        return file(fqName(typeEl) + '_' + callbacks);
    }

    private JavaFileObject file(String name) {
        try {
            return filer.apply(name);
        } catch (Exception e) {
            throw new IllegalStateException("Could not open file " + name, e);
        }
    }

    private static final String GENERATED = Generated.class.getSimpleName();

    private static final String PRESET_CALLBACKS = PresetCallbacks.class.getSimpleName();

    private static final String PRESET_CALLBACKS_INITIALIZER = PresetCallbacksInitializer.class.getSimpleName();

    private static final String ABSTRACT_OBJECT_WRITER = AbstractObjectWriter.class.getSimpleName();

    private static final String FIELD_EVENTS = FieldEvents.class.getSimpleName();

    private static final String SUPPLIER = Supplier.class.getSimpleName();

    private static final String JSON_RW = JsonRW.class.getSimpleName();

    private static final String CONSUMER = Consumer.class.getSimpleName();

    private static final String CALLBACKS = Callbacks.class.getSimpleName();

    private static final String FUNCTION = Function.class.getSimpleName();

    private static final String OBJECT_WRITER = ObjectWriter.class.getSimpleName();

    private static final String QUO = "\"";

    private static String quote(Object string) {
        return QUO + string + QUO;
    }

    private static String print(TypeMirror type) {
        return Stream.of(
                String.class,
                Character.class,
                Integer.class,
                Long.class,
                Double.class,
                Float.class,
                Short.class,
                Byte.class,
                Boolean.class
            )
            .filter(t ->
                t.getName().equals(type.toString()))
            .map(Class::getSimpleName)
            .findFirst()
            .orElseGet(type::toString);
    }

    private static void write(BufferedWriter bw, String... strs) {
        write(bw, Arrays.asList(strs));
    }

    private static void write(BufferedWriter bw, List<String> strs) {
        strs.forEach(str -> {
            try {
                write(bw, str);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to write line `" + str + "`", e);
            }
        });
    }

    private static void write(BufferedWriter bw, String str) {
        try {
            bw.write(str + "\n");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write line `" + str + "`", e);
        }
    }

    private static BufferedWriter writer(JavaFileObject javaFileObject) {
        try {
            return new BufferedWriter(javaFileObject.openWriter());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write to " + javaFileObject, e);
        }
    }

    private static String writerClassPlain(TypeElement te) {
        return simpleName(te) + "_Writer";
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               "pe=" + jsonRecordPackage + ", " +
               "te=" + jsonRecord + ", " +
               "types=" + jsonRecords + ", " +
               "enums=" + enums + ", " +
               "time=" + timestamp + ", " +
               "filer=" + filer + ", " +
               "elementUtils=" + elementUtils + ", " +
               "typeUtils=" + typeUtils + ']';
    }

}
