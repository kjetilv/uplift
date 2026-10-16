/// @jenesis.plugin uplift.json.gen
/// @jenesis.release 25
module uplift.json.samplegen {
    requires java.compiler;
    requires java.net.http;

    requires uplift.hash;
    requires uplift.json;
    requires uplift.json.anno;
    requires uplift.json.gen;

    exports com.github.kjetilv.uplift.json.samplegen;
}
