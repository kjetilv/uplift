/// Test module
///
/// @jenesis.test
module uplift.flogs.test {
    requires org.junit.jupiter.api;
    requires org.assertj.core;
    requires uplift.flogs;

    opens com.github.kjetilv.uplift.flogs.test to org.junit.platform.commons;
}
