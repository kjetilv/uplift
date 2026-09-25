/// @jenesis.alias jmh.core org.openjdk.jmh/jmh-core
/// @jenesis.pin org.openjdk.jmh/jmh-core 1.37
module uplift.synchttp.jmh {

    requires java.net.http;

    requires uplift.synchttp;

    requires jmh.core;
    requires io.netty.buffer;
    requires io.netty.codec;
    requires io.netty.codec.http;
    requires io.netty.common;
    requires io.netty.transport;
}
