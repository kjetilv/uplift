/// Test modjule
///
/// @jenesis.test
/// @jenesis.bom pin-test.properties
/// @jenesis.release 25
module uplift.util.test {
    requires uplift.util;
    requires org.assertj.core;
    requires org.junit.jupiter.api;

    opens com.github.kjetilv.uplift.util.test to org.junit.platform.commons;
}
