module uplift.edam {
    requires uplift.hash;
    requires uplift.util;
    requires jdk.incubator.vector;

    exports com.github.kjetilv.uplift.edam;
    exports com.github.kjetilv.uplift.edam.patterns;
    exports com.github.kjetilv.uplift.edam.throwables;

    exports com.github.kjetilv.uplift.edam.internal to uplift.edam.test;
}
