/// Test module
///
/// @jenesis.plugin uplift.json.gen
/// @jenesis.alias jmh.core org.openjdk.jmh/jmh-core
/// @jenesis.alias jmh.generator.annprocess org.openjdk.jmh/jmh-generator-annprocess
/// @jenesis.test
/// @jenesis.bom pin-test.properties
/// @jenesis.release 25
module uplift.json.jmh.test {
    requires com.fasterxml.jackson.annotation;
    requires jmh.core;
    requires org.junit.jupiter.api;
    requires org.assertj.core;
    requires tools.jackson.databind;
    requires uplift.hash;
    requires uplift.json;
    requires uplift.json.gen;
    requires uplift.json.mame;
    requires uplift.json.jmh;

    exports com.github.kjetilv.uplift.jmh.test;

    opens com.github.kjetilv.uplift.jmh.test to org.junit.platform.commons;
}
