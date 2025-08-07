module uplift.json {

    requires java.compiler;
    requires uplift.uuid;
    requires uplift.kernel;

    exports com.github.kjetilv.uplift.json;
    exports com.github.kjetilv.uplift.json.bytes;
    exports com.github.kjetilv.uplift.json.callbacks;
    exports com.github.kjetilv.uplift.json.io;
}