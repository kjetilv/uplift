/// @jenesis.plugin uplift.json.gen
/// @jenesis.plugin maven/org.openjdk.jmh/jmh-generator-annprocess
/// @jenesis.alias jmh.core org.openjdk.jmh/jmh-core
/// @jenesis.alias jmh.generator.annprocess org.openjdk.jmh/jmh-generator-annprocess
/// @jenesis.alias jopt.simple net.sf.jopt-simple/jopt-simple
/// @jenesis.alias commons.math3 org.apache.commons/commons-math3
/// @jenesis.pin org.openjdk.jmh/jmh-core 1.37
/// @jenesis.pin org.openjdk.jmh/jmh-generator-annprocess 1.37
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

    opens com.github.kjetilv.uplift.jmh;
}
