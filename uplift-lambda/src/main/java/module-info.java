/// @jenesis.plugin uplift.json.gen
module uplift.lambda {
    requires java.compiler;
    requires java.net.http;

    requires uplift.flogs;
    requires uplift.hash;
    requires uplift.kernel;
    requires uplift.s3;
    requires uplift.util;

    requires uplift.json;
    requires uplift.json.anno;
    requires uplift.json.gen;

    exports com.github.kjetilv.uplift.lambda;
}
