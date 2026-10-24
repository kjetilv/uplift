/// @jenesis.release 25
/// @jenesis.bom pin-aws.properties
/// @jenesis.exclude software.amazon.awssdk.auth org.slf4j/slf4j-api
/// @jenesis.exclude software.amazon.awssdk.regions org.slf4j/slf4j-api
/// @jenesis.exclude software.amazon.awssdk.services.cloudformation org.slf4j/slf4j-api
/// @jenesis.exclude software.amazon.awssdk.services.lambda org.slf4j/slf4j-api
module uplift.plugin.core {
    requires java.net.http;

    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.services.cloudformation;
    requires software.amazon.awssdk.services.lambda;

    exports com.github.kjetilv.uplift.plugins.core;
}
