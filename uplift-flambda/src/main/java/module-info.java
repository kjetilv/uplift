module uplift.flambda {
    requires java.net.http;
    requires org.junit.jupiter.api;
    requires uplift.flogs;
    requires uplift.hash;
    requires uplift.kernel;
    requires uplift.lambda;
    requires uplift.synchttp;
    requires uplift.util;

    requires static uplift.json;
    requires static uplift.json.gen;

    exports com.github.kjetilv.uplift.flambda;
}
