/// @jenesis.test
module uplift.edam.test {
    requires org.junit.jupiter.api;
    requires org.slf4j;
    requires uplift.edam;
    requires uplift.hash;

    opens com.github.kjetilv.uplift.edam.test to org.junit.platform.commons;
    opens com.github.kjetilv.uplift.edam.test.internal to org.junit.platform.commons;
    opens com.github.kjetilv.uplift.edam.test.patterns to org.junit.platform.commons;
    opens com.github.kjetilv.uplift.edam.test.ex to org.junit.platform.commons;
}
