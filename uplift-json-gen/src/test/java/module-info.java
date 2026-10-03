/// Test module
///
/// @jenesis.test
module uplift.json.gen.test {
    requires java.compiler;
    requires uplift.hash;
    requires uplift.json;
    requires java.net.http;
    requires uplift.json.anno;
    requires uplift.json.gen;
    requires org.junit.jupiter;
    requires org.junit.jupiter.api;
    requires uplift.flogs;
    requires org.assertj.core;

    opens com.github.kjetilv.uplift.json.gen.test to org.junit.platform.commons;
    opens com.github.kjetilv.uplift.json.gen.test.trie to org.junit.platform.commons;
}
