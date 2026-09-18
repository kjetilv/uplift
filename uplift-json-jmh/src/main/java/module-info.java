/// @jenesis.plugin uplift.json.gen
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
}
