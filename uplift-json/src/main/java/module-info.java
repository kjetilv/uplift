module uplift.uplift.json.main {

    requires java.compiler;
    requires uplift.uplift.uuid.main;
    requires java.desktop;
    requires org.graalvm.nativeimage;

    exports com.github.kjetilv.uplift.json;
    exports com.github.kjetilv.uplift.json.gen;
}