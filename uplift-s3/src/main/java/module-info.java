/// @jenesis.release 25
@SuppressWarnings("module")
module uplift.s3 {
    requires java.net.http;

    requires uplift.flogs;
    requires uplift.kernel;
    requires uplift.util;
    requires uplift.json;

    exports com.github.kjetilv.uplift.s3;

    exports com.github.kjetilv.uplift.s3.auth to uplift.s3.test;
    exports com.github.kjetilv.uplift.s3.util to uplift.s3.test;
}
