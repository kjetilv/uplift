module uplift.uplift.kernel.main {
    requires uplift.uplift.flogs.main;
    requires uplift.uplift.uuid.main;

    exports com.github.kjetilv.uplift.kernel;
    exports com.github.kjetilv.uplift.kernel.aws;
    exports com.github.kjetilv.uplift.kernel.http;
    exports com.github.kjetilv.uplift.kernel.io;
    exports com.github.kjetilv.uplift.kernel.util;
}