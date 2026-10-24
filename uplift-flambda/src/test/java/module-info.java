/// Test module
///
/// @jenesis.test
/// @jenesis.bom pin-test.properties
/// @jenesis.release 25
/// @jenesis.pin org.junit.jupiter 6.1.3 SHA-256/6b0a14075c74221de53047e363860b7d19fce802bebc152df2c35c4089c6c379
/// @jenesis.pin org.junit.jupiter/junit-jupiter 6.1.3 SHA-256/6b0a14075c74221de53047e363860b7d19fce802bebc152df2c35c4089c6c379
/// @jenesis.pin org.junit.jupiter/junit-jupiter-params 6.1.3 SHA-256/05c51cedba2c06a9770707391e006cee825876937dea580b94a265ce145d4939
/// @jenesis.pin org.junit.platform.commons 6.1.3 SHA-256/a4774ae923c109544034aeae7c762cf017fbcdd1342403ba90e0921984b608c3
module uplift.flambda.test {
    requires java.net.http;
    requires uplift.flogs;
    requires uplift.hash;
    requires uplift.kernel;
    requires uplift.synchttp;
    requires uplift.util;
    requires uplift.json;
    requires uplift.json.gen;
    requires uplift.lambda;
    requires uplift.flambda;

    requires org.assertj.core;
    requires org.junit.platform.commons;
    requires org.junit.jupiter;
    requires org.junit.jupiter.api;

    opens com.github.kjetilv.uplift.flambda.test;
}
