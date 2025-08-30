module uplift.kernel {
    requires uplift.flogs;
    requires uplift.uuid;
    requires uplift.util;

    exports com.github.kjetilv.uplift.kernel;
    exports com.github.kjetilv.uplift.kernel.aws;
    exports com.github.kjetilv.uplift.kernel.http;
    exports com.github.kjetilv.uplift.kernel.io;
}