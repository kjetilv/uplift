package com.github.kjetilv.uplift.asynchttp;

import com.github.kjetilv.uplift.util.RuntimeCloseable;

import java.io.Closeable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;

public interface IOServer extends RuntimeCloseable {

    String ALL = "0.0.0.0";

    int MINIMUM_REQUEST_SIZE = 1024;

    static InetAddress getInetAddress() {
        try {
            return InetAddress.getByName(ALL);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to resolve bind address", e);
        }
    }

    static int resolvePort(Integer port) {
        return port == null || port <= 0 ? 0 : port;
    }

    static InetSocketAddress resolveAddress(Integer port) {
        return new InetSocketAddress(
            getInetAddress(),
            resolvePort(port)
        );
    }

    default void awaitActive(Duration timeout) {
    }

    int port();

    @Override
    void close();

    void join();

    static int requestBufferSize(int requestBufferSize) {
        return requestBufferSize > 0 ? requestBufferSize : MINIMUM_REQUEST_SIZE;
    }
}
