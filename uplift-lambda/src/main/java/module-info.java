module uplift.uplift.lambda.main {
    requires java.compiler;
    requires java.net.http;
    requires uplift.uplift.json.main;
    requires uplift.uplift.flogs.main;
    requires uplift.uplift.kernel.main;
    requires uplift.uplift.uuid.main;

    exports com.github.kjetilv.uplift.lambda;
}