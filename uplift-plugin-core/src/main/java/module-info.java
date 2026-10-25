/// @jenesis.release 25
/// @jenesis.bom pin-aws.properties
/// @jenesis.exclude software.amazon.awssdk.auth org.slf4j/slf4j-api
/// @jenesis.exclude software.amazon.awssdk.regions org.slf4j/slf4j-api
/// @jenesis.exclude software.amazon.awssdk.services.cloudformation org.slf4j/slf4j-api
/// @jenesis.exclude software.amazon.awssdk.services.lambda org.slf4j/slf4j-api
/// @jenesis.pin io.netty/netty-buffer 4.2.19.Final SHA-256/863f09b46cfe7d83872c9499086b9681cd9878c30a319d9f1f3a681bbfb0b12f
/// @jenesis.pin io.netty/netty-codec 4.2.19.Final SHA-256/9da978357522895ed6141e14c81f776173206f6cdd72e92b637c60e6e624735b
/// @jenesis.pin io.netty/netty-codec-base 4.2.19.Final SHA-256/5702c8097cc81e939dd12e5b39ac2e81f83737c320f6f4d88553577842f9ceb6
/// @jenesis.pin io.netty/netty-codec-compression 4.2.19.Final SHA-256/8935c51c7217780ae56d178c938b50ea5729b1e088cf3c2568674e331665554c
/// @jenesis.pin io.netty/netty-codec-http 4.2.19.Final SHA-256/7c28809a4f527746fbce2369f5ac5d5fcef45cfe9da21aa3dd9d3f6bdf00cbe0
/// @jenesis.pin io.netty/netty-codec-http2 4.2.19.Final SHA-256/f45756b12a75b907e98cd0158dc947308ca463cd3d27b9035d11a025a4fdbc4d
/// @jenesis.pin io.netty/netty-codec-marshalling 4.2.19.Final SHA-256/5b24976fda31fd52a85fb7f79f2a5867a0416f68f7b03a9d6ea062b2959d50f7
/// @jenesis.pin io.netty/netty-codec-protobuf 4.2.19.Final SHA-256/f4d10c51f9237d4cffe78978dee2d2473c0eacdb021f0c04a0cc4f123e01f15d
/// @jenesis.pin io.netty/netty-common 4.2.19.Final SHA-256/22fa74761f6e35c8c88972f35cf65664566b922dd73d3424528e97c34278fa79
/// @jenesis.pin io.netty/netty-handler 4.2.19.Final SHA-256/8b469cf23ddf21ba84291f4dff470d2277a28deac95e7084b0ff2ce853e58276
/// @jenesis.pin io.netty/netty-resolver 4.2.19.Final SHA-256/3ded47536faeb3c4c544a84871d82f461511589afb2a892a180ca425f281363e
/// @jenesis.pin io.netty/netty-transport 4.2.19.Final SHA-256/d82d4e200456b8c5d8a1647a3724603f80616e5b35b9ada6e7a20d048f175075
/// @jenesis.pin io.netty/netty-transport-classes-epoll 4.2.19.Final SHA-256/4d96ba8dd98ace998f6ae7bfe53ae9e581bd92ad2f7ebe5bd994d1a24306db7e
/// @jenesis.pin io.netty/netty-transport-native-unix-common 4.2.19.Final SHA-256/341e91b8e69e6b7d92c353c845ffd91f34e921b7186254c176a64c7089b3e721
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
