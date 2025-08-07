module uplift.lambda {
    requires java.compiler;
    requires java.net.http;
    requires uplift.json;
    requires uplift.json.gen;
    requires uplift.flogs;
    requires uplift.kernel;
    requires uplift.uuid;

    exports com.github.kjetilv.uplift.lambda;
}