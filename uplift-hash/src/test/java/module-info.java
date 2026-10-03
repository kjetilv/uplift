/// Test module
///
/// @jenesis.test
module uplift.hash.test {
    requires uplift.hash;
    requires org.junit.jupiter.api;
    requires uplift.util;

    opens com.github.kjetilv.uplift.hash.test to org.junit.platform.commons;
}
