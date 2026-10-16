/// @jenesis.test
/// @jenesis.release 25
module uplift.lambda.test {
    requires java.net.http;
    requires org.junit.jupiter.api;
    requires org.assertj.core;
    requires uplift.util;
    requires uplift.lambda;

    opens com.github.kjetilv.uplift.lambda.test to org.junit.platform.commons;
}
