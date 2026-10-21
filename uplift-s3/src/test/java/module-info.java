/// @jenesis.test
/// @jenesis.bom pin-test.properties
/// @jenesis.release 25
@SuppressWarnings("module")
module uplift.s3.test {
    requires java.net.http;
    requires org.junit.jupiter.api;
    requires org.assertj.core;
    requires uplift.kernel;
    requires uplift.s3;

    opens com.github.kjetilv.uplift.s3.test to org.junit.platform.commons;
}
