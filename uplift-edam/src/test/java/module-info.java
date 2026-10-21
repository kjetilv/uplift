/// Test module
///
/// @jenesis.test
/// @jenesis.bom pin-test.properties
/// @jenesis.release 25
module uplift.edam.test {
    requires org.junit.jupiter.api;
    requires uplift.edam;
    requires uplift.hash;
    requires uplift.flogs;

    opens com.github.kjetilv.uplift.edam.test to org.junit.platform.commons;
    opens com.github.kjetilv.uplift.edam.test.internal to org.junit.platform.commons;
    opens com.github.kjetilv.uplift.edam.test.patterns to org.junit.platform.commons;
    opens com.github.kjetilv.uplift.edam.test.ex to org.junit.platform.commons;
}
