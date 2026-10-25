/// @jenesis.release 25
/// @jenesis.bom pin-aws.properties
/// @jenesis.bom pin-netty.properties
/// @jenesis.exclude software.amazon.awssdk.auth org.slf4j/slf4j-api
/// @jenesis.exclude software.amazon.awssdk.regions org.slf4j/slf4j-api
/// @jenesis.exclude software.amazon.awssdk.services.cloudformation org.slf4j/slf4j-api
/// @jenesis.exclude software.amazon.awssdk.services.lambda org.slf4j/slf4j-api
/// @jenesis.pin software.amazon.awssdk.auth 2.57.0 SHA-256/73dadb79017c04de6359506575e5fc2e36e14264ff7013ea616acd1da5c87582
/// @jenesis.pin software.amazon.awssdk.regions 2.57.0 SHA-256/8984377c8954164a989919925738efe88c27fa00ec78d7d519ad3c674f56ef25
/// @jenesis.pin software.amazon.awssdk.services.cloudformation 2.57.0 SHA-256/2cf8677b0b58b4db730d7d4a3178808013309d08f088bf51ae8618ccaf9906a2
/// @jenesis.pin software.amazon.awssdk.services.lambda 2.57.0 SHA-256/da6d48007bdb9b628ad592d389f6ad73173ad2b061e3ed91befe6a252d729589
module uplift.plugin.core {
    requires java.net.http;

    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.services.cloudformation;
    requires software.amazon.awssdk.services.lambda;

    exports com.github.kjetilv.uplift.plugins.core;
}
