/// @jenesis.release 25
module uplift.synchttp {
    requires jdk.incubator.vector;
    requires uplift.flogs;
    requires uplift.hash;
    requires uplift.kernel;
    requires uplift.util;

    requires java.management;

    exports com.github.kjetilv.uplift.synchttp;
    exports com.github.kjetilv.uplift.synchttp.rere;
    exports com.github.kjetilv.uplift.synchttp.write;
    exports com.github.kjetilv.uplift.synchttp.read;
}
