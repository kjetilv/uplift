/// Test module
///
/// @jenesis.test
/// @jenesis.bom pin-test.properties
/// @jenesis.release 25
module hello.web.service.test {
    requires java.net.http;

    requires hello.web.service;

    requires uplift.lambda;
    requires uplift.flambda;

    requires org.junit.jupiter.api;
    requires org.assertj.core;

    opens uplift.examples.helloweb.test to org.junit.platform.commons;
}
