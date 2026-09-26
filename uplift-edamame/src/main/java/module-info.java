module uplift.edamame {
    requires uplift.util;
    requires uplift.hash;

    exports com.github.kjetilv.uplift.edamame;
    exports com.github.kjetilv.uplift.edamame.impl to uplift.edamame.test;
}
