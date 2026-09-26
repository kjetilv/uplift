module uplift.edamame.test {
    requires org.junit.jupiter.api;
    requires uplift.edamame;
    requires uplift.hash;
    requires uplift.util;
    requires org.assertj.core;

    opens com.github.kjetilv.uplift.edamame.test to org.junit.platform.commons;
}
