package com.github.kjetilv.uplift.synchttp;

import com.github.kjetilv.uplift.util.RuntimeCloseable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public interface Server extends RuntimeCloseable {

    static Server create() {
        return create(0);
    }

    static Server create(Integer port) {
        return create(new InetSocketAddress(
            InetAddress.getLoopbackAddress(),
            resolvePort(port)
        ));
    }

    static Server create(InetSocketAddress address) {
        return new DefaultServer(address);
    }

    default URI uri() {
        var address = address();
        return URI.create("http://%s:%d".formatted(address.getHostName(), address.getPort()));
    }

    InetSocketAddress address();

    void join();

    Server run(Processor processor);

    private static int resolvePort(Integer port) {
        return port == null || port <= 0 ? 0 : port;
    }

    interface Processor extends RuntimeCloseable {

        boolean process(
            ReadableByteChannel in,
            WritableByteChannel out
        );
    }
}
