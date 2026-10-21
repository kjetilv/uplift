/// Test module
///
/// @jenesis.test
/// @jenesis.bom pin-test.properties
/// @jenesis.release 25
module uplift.json.mame.test {
    requires org.assertj.core;
    requires org.junit.jupiter.api;
    requires uplift.hash;
    requires uplift.json;
    requires uplift.json.mame;

    opens com.github.kjetilv.uplift.json.mame.test to org.junit.platform.commons;
}
