import build.jenesis.BuildModuleFactory;
import com.kjetilv.github.uplift.plugins.jenesis.UpliftFactory;

/// @jenesis.bom pin-jenesis.properties
/// @jenesis.release 25
/// @jenesis.bom pin-aws.properties
/// @jenesis.bom pin-jenesis.properties
/// @jenesis.bom pin-jenesis.properties
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
/// @jenesis.pin org.apache.httpcomponents.client5/httpclient5 5.6.4 SHA-256/bdef5f8841145cd76c4c605ff301bc840eea0d5292332b7b892a93316c0d8ee1
/// @jenesis.pin org.apache.httpcomponents.core5/httpcore5 5.4.3 SHA-256/18bfbbabb478dfb67f31aeaf428c387f3c3df654582e1309f708ee1f3086830a
/// @jenesis.pin org.apache.httpcomponents.core5/httpcore5-h2 5.4.3 SHA-256/c7db7026b8e2dea39132b04a6069f6671e2858309b20a146ec5c7dd6ed73a0b6
/// @jenesis.pin org.reactivestreams/reactive-streams 1.0.4 SHA-256/f75ca597789b3dac58f61857b9ac2e1034a68fa672db35055a8fb4509e325f28
module uplift.jenesis.plugin {
    requires uplift.plugin.core;
    requires uplift.flogs;
    requires build.jenesis;

    provides BuildModuleFactory with UpliftFactory;
}
