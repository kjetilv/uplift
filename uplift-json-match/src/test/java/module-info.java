// @jenesis.test
module uplift.json.match.test {
    requires org.junit.jupiter.api;
    requires uplift.json;
    requires uplift.json.match;
    requires org.assertj.core;

    opens com.github.kjetilv.uplift.json.match.test to org.junit.platform.commons;
}
