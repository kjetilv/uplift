/**
 * @jenesis.plugin uplift.json.gen
 */
module uplift.lambda {
    requires java.net.http;
    requires uplift.flogs;
    requires uplift.kernel;
    requires uplift.util;
    requires uplift.hash;
    requires uplift.s3;
    requires uplift.json;
    requires uplift.json.gen;
    requires java.compiler;
    requires uplift.json.anno;

    exports com.github.kjetilv.uplift.lambda;
}
