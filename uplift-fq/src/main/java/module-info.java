module uplift.fq {
    requires uplift.flogs;
    requires uplift.util;

    requires jdk.incubator.vector;

    exports com.github.kjetilv.uplift.fq;

    exports com.github.kjetilv.uplift.fq.flows to uplift.fq.test;
    exports com.github.kjetilv.uplift.fq.io to uplift.fq.test;
    exports com.github.kjetilv.uplift.fq.partitions to uplift.fq.test;
    exports com.github.kjetilv.uplift.fq.paths to uplift.fq.test;
    exports com.github.kjetilv.uplift.fq.paths.bytes to uplift.fq.test;
    exports com.github.kjetilv.uplift.fq.paths.ffm to uplift.fq.test;
}
