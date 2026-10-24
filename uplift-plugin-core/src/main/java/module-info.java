/// @jenesis.release 25
/// @jenesis.bom pin-aws.properties
/// @jenesis.pin io.netty/netty-buffer 4.2.19.Final SHA-256/5bef3ea2b9e80c5e8d03151ca1609bd75fd1e264863bf3e2f8d538392c0f2c38
/// @jenesis.pin io.netty/netty-codec 4.2.19.Final SHA-256/21b9b7717da9ed7034c7a9ef4fcea4a08825e8f52d775da4d898364edf99327f
/// @jenesis.pin io.netty/netty-codec-http 4.2.19.Final SHA-256/7d9000e53c8e37504be42b8dd7b646a7d1c73ad2cf801527fe3c6301880fe06f
/// @jenesis.pin io.netty/netty-codec-http2 4.2.19.Final SHA-256/c7521231535c252903d9e424e162f789b5b55b0b0799628ea466a9b3daf49dae
/// @jenesis.pin io.netty/netty-common 4.2.19.Final SHA-256/b4ebb8fa44cd5790731928a83fd0f25a530e41c407c093bef4a0196db30b693f
/// @jenesis.pin io.netty/netty-handler 4.2.19.Final SHA-256/a79fb814e029db17da82baea25a0340172af2c59f107fd20b394c1fa28680f21
/// @jenesis.pin io.netty/netty-resolver 4.2.19.Final SHA-256/50c338a0e5c9997513e738f0fcb47d579a9e8852d317ba367f5d141e210187c0
/// @jenesis.pin io.netty/netty-transport 4.2.19.Final SHA-256/6c064c1cae4d410b920abfc07959a49db1b4778a9547614373e97d31cab0cad6
/// @jenesis.pin io.netty/netty-transport-classes-epoll 4.2.19.Final SHA-256/a3caf181f3af1665a7cd8b5d409ffe425308da49bf9297482df3942411a7707c
/// @jenesis.pin io.netty/netty-transport-native-unix-common 4.2.19.Final SHA-256/8d1ada8f185d6f6da4cd79f9daeef39f1db49908cfec8bc2a1fb602458aa2341
/// @jenesis.pin software.amazon.awssdk.auth 2.57.0 SHA-256/73dadb79017c04de6359506575e5fc2e36e14264ff7013ea616acd1da5c87582
/// @jenesis.pin software.amazon.awssdk.regions 2.57.0 SHA-256/8984377c8954164a989919925738efe88c27fa00ec78d7d519ad3c674f56ef25
/// @jenesis.pin software.amazon.awssdk.services.cloudformation 2.57.0 SHA-256/2cf8677b0b58b4db730d7d4a3178808013309d08f088bf51ae8618ccaf9906a2
/// @jenesis.pin software.amazon.awssdk.services.lambda 2.57.0 SHA-256/da6d48007bdb9b628ad592d389f6ad73173ad2b061e3ed91befe6a252d729589
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
