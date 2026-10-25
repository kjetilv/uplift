/// @jenesis.plugin uplift.json.gen
/// @jenesis.plugin maven/org.openjdk.jmh/jmh-generator-annprocess
/// @jenesis.alias jmh.core org.openjdk.jmh/jmh-core
/// @jenesis.alias jmh.generator.annprocess org.openjdk.jmh/jmh-generator-annprocess
/// @jenesis.alias jopt.simple net.sf.jopt-simple/jopt-simple
/// @jenesis.alias commons.math3 org.apache.commons/commons-math3
/// @jenesis.bom pin-jmh.properties
/// @jenesis.bom pin-jackson.properties
/// @jenesis.release 25
/// @jenesis.pin plugin/maven/net.sf.jopt-simple/jopt-simple 5.0.4 SHA-256/df26cc58f235f477db07f753ba5a3ab243ebe5789d9f89ecf68dd62ea9a66c28
/// @jenesis.pin plugin/maven/org.apache.commons/commons-math3 3.6.1 SHA-256/1e56d7b058d28b65abd256b8458e3885b674c1d588fa43cd7d1cbb9c7ef2b308
/// @jenesis.pin plugin/maven/org.openjdk.jmh/jmh-core 1.37 SHA-256/dc0eaf2bbf0036a70b60798c785d6e03a9daf06b68b8edb0f1ba9eb3421baeb3
/// @jenesis.pin plugin/maven/org.openjdk.jmh/jmh-generator-annprocess 1.37 SHA-256/6a5604b5b804e0daca1145df1077609321687734a8b49387e49f10557c186c77
/// @jenesis.pin tools.jackson.core 3.2.4 SHA-256/494b9f5fccf008042d5a72c5bf00629b144c0ba7362a9864cbb02234c9772651
module uplift.json.jmh {
    requires java.net.http;

    requires uplift.json.gen;
    requires tools.jackson.core;
    requires tools.jackson.databind;

    requires uplift.json.anno;
    requires uplift.json.mame;
    requires uplift.json;
    requires uplift.hash;
    requires uplift.util;

    requires jmh.core;
    requires jmh.generator.annprocess;
    requires java.compiler;
    requires jdk.incubator.vector;

    exports com.github.kjetilv.uplift.jmh to uplift.json.jmh.test;
}
