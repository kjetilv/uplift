/// @jenesis.release 25
module uplift.plugin.core {
    requires java.net.http;

    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.services.cloudformation;
    requires software.amazon.awssdk.services.lambda;

    exports com.github.kjetilv.uplift.plugins.core;
}
