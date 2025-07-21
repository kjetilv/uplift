module uplift.json {

    requires java.compiler;
    requires uplift.uuid;
    requires java.desktop;
    requires org.graalvm.nativeimage;
    requires flopp.flopp.kernel.main;

    exports com.github.kjetilv.uplift.json;
    exports com.github.kjetilv.uplift.json.gen;
    exports com.github.kjetilv.uplift.json.anno;
}