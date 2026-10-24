/// Test module
///
/// @jenesis.plugin uplift.json.gen
/// @jenesis.alias jmh.core org.openjdk.jmh/jmh-core
/// @jenesis.alias jmh.generator.annprocess org.openjdk.jmh/jmh-generator-annprocess
/// @jenesis.test
/// @jenesis.bom pin-test.properties
/// @jenesis.release 25
/// @jenesis.pin com.fasterxml.jackson.annotation 2.22 SHA-256/21ddb598807d3a51a876704eb979d9296e1c6a6f47ab1826ff88c6d6a127a2d0
/// @jenesis.pin com.fasterxml.jackson.core/jackson-annotations 2.22 SHA-256/21ddb598807d3a51a876704eb979d9296e1c6a6f47ab1826ff88c6d6a127a2d0
/// @jenesis.pin net.sf.jopt-simple/jopt-simple 5.0.4 SHA-256/df26cc58f235f477db07f753ba5a3ab243ebe5789d9f89ecf68dd62ea9a66c28
/// @jenesis.pin org.apache.commons/commons-math3 3.6.1 SHA-256/1e56d7b058d28b65abd256b8458e3885b674c1d588fa43cd7d1cbb9c7ef2b308
/// @jenesis.pin org.openjdk.jmh/jmh-core 1.37 SHA-256/dc0eaf2bbf0036a70b60798c785d6e03a9daf06b68b8edb0f1ba9eb3421baeb3
/// @jenesis.pin org.openjdk.jmh/jmh-generator-annprocess 1.37 SHA-256/6a5604b5b804e0daca1145df1077609321687734a8b49387e49f10557c186c77
/// @jenesis.pin tools.jackson.core/jackson-core 3.2.3 SHA-256/a0c17e7e665a5e95c82f5fb45c0fec2ae3dc556c80f0016452c9c84cd23f3817
/// @jenesis.pin tools.jackson.core/jackson-databind 3.2.3 SHA-256/235b72436af983857e44efa42449f4f0a2c571e69d6c5d9dd1253ae7d192dcf2
/// @jenesis.pin tools.jackson.databind 3.2.3 SHA-256/235b72436af983857e44efa42449f4f0a2c571e69d6c5d9dd1253ae7d192dcf2
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
