/// Test module
///
/// @jenesis.test
module uplift.fq.test {

    requires uplift.fq;
    requires uplift.hash;
    requires uplift.json;
    requires uplift.json.mame;
    requires uplift.util;
    requires org.assertj.core;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;

    opens com.github.kjetilv.uplift.fq.test.partitions to org.junit.platform.commons;
    opens com.github.kjetilv.uplift.fq.test.paths to org.junit.platform.commons;
    opens com.github.kjetilv.uplift.fq.test.paths.bytes to org.junit.platform.commons;
}
