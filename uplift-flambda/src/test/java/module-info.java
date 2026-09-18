module uplift.flambda.test {
    requires java.net.http;
    requires org.jspecify;
    requires uplift.flogs;
    requires uplift.hash;
    requires uplift.kernel;
    requires uplift.synchttp;
    requires uplift.util;
    requires static uplift.json;
    requires static uplift.json.gen;
    requires uplift.lambda;
    requires uplift.flambda;

    requires org.assertj.core;
    requires org.junit.platform.commons;
    requires org.junit.jupiter;
    requires org.junit.jupiter.api;

    opens com.github.kjetilv.uplift.flambda.test;
}
