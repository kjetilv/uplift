/// Test module
///
/// @jenesis.test
module uplift.json.test {
    requires uplift.json;
    requires org.junit.jupiter.api;
    requires org.assertj.core;

    opens com.github.kjetilv.uplift.json.test to org.junit.platform.commons;
    opens com.github.kjetilv.uplift.json.test.io to org.junit.platform.commons;
    opens com.github.kjetilv.uplift.json.test.events to org.junit.platform.commons;
}
