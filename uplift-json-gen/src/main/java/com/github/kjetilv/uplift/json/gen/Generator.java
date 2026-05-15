package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.FieldEvents;
import com.github.kjetilv.uplift.json.MapWriter;
import com.github.kjetilv.uplift.json.ObjectWriter;
import com.github.kjetilv.uplift.json.anno.JsonRecord;

import static com.github.kjetilv.uplift.json.gen.GenUtils.*;

record Generator(
    PackageElement pe,
    TypeElement te,
    Collection<? extends Element> types,
    Collection<? extends Element> enums,
    String time,
    Function<String, JavaFileObject> filer,
    Elements elementUtils,
    Types typeUtils
) {

    void writeCallbacks() {
        var file = callbackFile(te);
        var unqualifiedName = unqTypeName();
        try (var bw = writer(file)) {
            write(
                bw,
                "package " + pe.getQualifiedName() + ";",
                "",
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
                "/// Generated at " + time + " by " + System.getProperty("user.name") + " using uplift",
                "@" + GENERATED + "(",
                "    value = \"" + JsonRecordProcessor.class.getName() + "\",",
                "    date = \"" + time + "\",",
                "    comments = \"Callbacks for " + unqualifiedName + "\"",
                ")",
                "final class " + callbacksClassPlain(te) + " {",
                "",
                "    static " + PRESET_CALLBACKS + "<" + builderClassPlain(te) + ", " + unqualifiedName + "> create(",
                "        " + CONSUMER + "<" + unqualifiedName + "> onDone",
                "    ) {",
                "        return create(null, onDone);",
                "    }",
                "",
                "    static " + PRESET_CALLBACKS + "<" + builderClassPlain(te) + ", " + unqualifiedName + "> create(",
                "        " + CALLBACKS + " parent, ",
                "        " + CONSUMER + "<" + unqualifiedName + "> onDone",
                "    ) {",
                "        return new " + PRESET_CALLBACKS + "<>(",
                "            " + builderClassPlain(te) + ".create(),",
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
                "    static final " + PRESET_CALLBACKS_INITIALIZER + "<" + builderClassPlain(te) + ", " + unqualifiedName + "> PRESETS;"
            );
            write(bw, "");
            write(bw, "    static {");
            write(bw, "        PRESETS = new " + PRESET_CALLBACKS_INITIALIZER + "<>(");
            write(bw, "            " + te.getQualifiedName() + ".class");
            write(bw, "        );");
            write(
                bw,
                te.getRecordComponents()
                    .stream()
                    .filter(element ->
                        element.getKind() == ElementKind.RECORD_COMPONENT)
                    .map(element ->
                        RecordAttribute.create(element, types, enums).callbackHandler(te))
                    .map(event ->
                        "        PRESETS." + event + ";"
                    )
                    .toList()
            );

            var recordAttributes = te.getRecordComponents()
                .stream()
                .filter(element ->
                    element.getKind() == ElementKind.RECORD_COMPONENT)
                .map(element ->
                    RecordAttribute.create(element, types, enums))
                .toList();

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
            throw new IllegalStateException("Failed to write callbacks for " + te, e);
        }
    }

    boolean isRoot() {
        var annotation = te.getAnnotation(JsonRecord.class);
        return annotation != null && annotation.root();
    }

    void writeRW() {
        var unqualifiedName = unqTypeName();
        var file = factoryFile(pe, te);
        try (var bw = writer(file)) {
            write(
                bw,
                "package " + pe.getQualifiedName() + ";",
                "",
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
                "/// Generated @ " + time + " by " + System.getProperty("user.name") + " using uplift",
                "@" + GENERATED + "(",
                "    value = \"" + JsonRecordProcessor.class.getName() + "\",",
                "    date = \"" + time + "\",",
                "    comments = \"Reading and writing " + unqualifiedName + "\"",
                ")",
                "public final class " + factoryClass(te) + " implements " + JSON_RW + "<" + unqualifiedName + "> {",
                "",
                "    public static " + JSON_RW + "<" + unqualifiedName + "> INSTANCE = new " + factoryClass(te) + "();",
                "",
                "    @Override",
                "    public " + FUNCTION + "<" + CONSUMER + "<" + unqualifiedName + ">, " + CALLBACKS + "> callbacks() {",
                "        return " + callbacksClassPlain(te) + "::create;",
                "    }",
                "",
                "    @Override",
                "    public " + CALLBACKS + " callbacks(" + CONSUMER + "<" + unqualifiedName + "> onDone) {",
                "        return " + callbacksClassPlain(te) + ".create(onDone);",
                "    }",
                "",
                "    @Override",
                "    public " + OBJECT_WRITER + "<" + unqualifiedName + "> objectWriter() {",
                "        return new " + writerClassPlain(te) + "();",
                "    }",
                "",
                "    private " + factoryClass(te) + "() {",
                "    }",
                "}"
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write factory for " + te, e);
        }

    }

    Map<String, ?> jsonSchema() {
        Map<String, ?> type = te.getRecordComponents()
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
        var file = writerFile(te);
        try (var bw = writer(file)) {
            var unqualifiedName = unqTypeName();
            write(
                bw,
                "package " + pe.getQualifiedName() + ";",
                "",
                importType(Generated.class),
                "",
                importType(AbstractObjectWriter.class),
                importType(FieldEvents.class),
                "",
                "/// Writer for [" + unqualifiedName + "]",
                "///",
                "/// Generated at " + time + " by " + System.getProperty("user.name") + " using uplift",
                "@" + GENERATED + "(",
                "    value = \"" + JsonRecordProcessor.class.getName() + "\",",
                "    date = \"" + time + "\",",
                "    comments = \"Writer for " + unqualifiedName + "\"",
                ")",
                "final class " + writerClassPlain(te),
                "    extends " + ABSTRACT_OBJECT_WRITER + "<" + unqualifiedName + ">",
                "{",
                "    protected " + FIELD_EVENTS + " doWrite(",
                "        " + unqualifiedName + " " + variableName(te) + ", ",
                "        " + FIELD_EVENTS + " events",
                "    ) {",
                "        return events"
            );
            te.getRecordComponents()
                .stream()
                .map(el ->
                    RecordAttribute.create(el, types, enums))
                .forEach(attr ->
                    write(
                        bw,
                        "            ." + writeCall(attr, te)
                    ));
            write(
                bw,
                "        ;",
                "    }",
                "}"
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write callbacks for " + te, e);
        }
    }

    void writeBuilder() {
        var file = builderFile(te);
        var setters = te.getRecordComponents()
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

        var adders = te.getRecordComponents()
            .stream()
            .flatMap(element ->
                listType(element, types, enums)
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

        List<String> creatorMeat = te.getRecordComponents()
            .stream()
            .map(el ->
                "            " + fieldName(el) + ",")
            .collect(Collectors.toCollection(LinkedList::new));
        var last = creatorMeat.removeLast();
        creatorMeat.addLast(last.substring(0, last.length() - 1));

        var creatorEnd = List.of(
            "        );",
            "    }"
        );

        try (var bw = writer(file)) {
            write(
                bw,
                "package " + pe.getQualifiedName() + ";",
                "",
                importType(Supplier.class),
                importType(Generated.class),
                "",
                "/// Builder for [" + unqualifiedName + "]",
                "///",
                "/// Generated at " + time + " by " + System.getProperty("user.name") + " using uplift",
                "@" + GENERATED + "(",
                "    value = \"" + JsonRecordProcessor.class.getName() + "\",",
                "    date = \"" + time + "\",",
                "    comments = \"Builder for " + unqualifiedName + "\"",
                ")",
                "final class " + builderClassPlain(te) + " implements " + SUPPLIER + "<" + unqualifiedName + "> {",
                "",
                "    static " + builderClassPlain(te) + " create() {",
                "        return new " + builderClassPlain(te) + "();",
                "    }",
                "",
                "    private " + builderClassPlain(te) + "() {",
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
            throw new IllegalStateException("Failed to write builder for " + te, e);
        }
    }

    private String writeCall(RecordAttribute recordAttribute, TypeElement te) {
        var attribute = recordAttribute.attribute();
        var listType = listType(attribute, types, enums);
        var isEnum = isType(attribute, enums) || isListType(attribute, enums);
        var isRoot = isType(attribute, types) || isListType(attribute, types);
        var isMap = isMap(attribute);
        var convert = !isMap && !isRoot && (isEnum || listType.map(BaseType::of)
            .orElseGet(() -> BaseType.of(attribute))
            .requiresConversion());
        var name = isRoot ? "object"
            : isMap ? "map"
                : isEnum ? "string"
                    : listType.map(BaseType::of).orElseGet(() -> BaseType.of(attribute)).methodName();
        return name +
               listType.map(_ -> "Array").orElse("") +
               "(" +
               quote(attribute.getSimpleName()) + ", " + variableName(te) + "." + attribute.getSimpleName() + "()" +
               (convert ? ", this::value)"
                   : isRoot ? ", new " + listType
                       .map((String listTypeName) ->
                           writerClass(attribute, listTypeName))
                       .orElseGet(getStringSupplier(attribute)) + "())"
                       : isMap ? ", new " + MapWriter.class.getName() + "())"
                           : ")");
    }

    private Supplier<String> getStringSupplier(RecordComponentElement attribute) {
        return () -> writerClass(attribute, attribute.asType().toString());
    }

    private String writerClass(RecordComponentElement attribute, String name) {
        var packageElement = packageEl(attribute);
        var prefix = packageElement.toString();
        return name.substring(prefix.length() + 1)
                   .replace('.', '_') + "_Writer";
    }

    private Map<String, Object> jsonType(RecordComponentElement element) {
        return listType(element, types, enums)
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
        return isMap(element) ? "object"
            : isType(element, types) || isMap(element) ? "object"
                : isType(element, enums) ? "string"
                    : isListType(element, enums) || isListType(element, types) ? "array"
                        : baseJsonType(element);
    }

    private String unqTypeName() {
        var name = te.getQualifiedName();
        var prefix = pe.getQualifiedName().toString();
        var fullName = name.toString();
        return fullName.startsWith(prefix)
            ? fullName.substring(prefix.length() + 1)
            : fullName;
    }

    private JavaFileObject factoryFile(PackageElement packageEl, TypeElement typeEl) {
        return file(factoryClassQ(packageEl, typeEl));
    }

    private JavaFileObject callbackFile(TypeElement typeEl) {
        return classFileName(typeEl, "Callbacks");
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

    private static BufferedWriter writer(JavaFileObject builder) {
        try {
            return new BufferedWriter(builder.openWriter());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write to " + builder, e);
        }
    }

    private static String writerClassPlain(TypeElement te) {
        return simpleName(te) + "_Writer";
    }
}
