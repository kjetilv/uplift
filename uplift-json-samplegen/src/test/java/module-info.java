/// @jenesis.test
/// @jenesis.release 25
module uplift.json.samplegen.test {
    requires java.compiler;
    requires java.net.http;
    requires org.junit.jupiter.api;
    requires org.assertj.core;
    requires uplift.json;
    requires uplift.json.anno;
    requires uplift.json.gen;
    requires uplift.json.mame;
    requires uplift.synchttp;
    requires uplift.json.samplegen;

    opens com.github.kjetilv.uplift.json.samplegen.test to org.junit.platform.commons;
}
