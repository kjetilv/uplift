@SuppressWarnings("module")
module uplift.s3 {
    requires java.net.http;

    requires uplift.flogs;
    requires uplift.kernel;
    requires uplift.util;
    requires uplift.json;

    exports com.github.kjetilv.uplift.s3;
}
