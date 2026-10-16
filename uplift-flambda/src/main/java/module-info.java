/// @jenesis.main com.github.kjetilv.uplift.flambda.FlambdaServer
/// @jenesis.release 25
module uplift.flambda {
    requires java.net.http;
    requires org.junit.jupiter.api;
    requires uplift.flogs;
    requires uplift.hash;
    requires uplift.kernel;
    requires uplift.lambda;
    requires uplift.synchttp;
    requires uplift.util;

    requires uplift.json;
    requires uplift.json.gen;

    exports com.github.kjetilv.uplift.flambda;

    //noinspection JavaModuleDefinition
    opens com.github.kjetilv.uplift.flambda to org.graalvm.nativeimage.guest.staging;
}
