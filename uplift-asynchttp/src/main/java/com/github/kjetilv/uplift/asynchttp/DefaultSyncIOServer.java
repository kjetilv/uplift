package com.github.kjetilv.uplift.asynchttp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

class DefaultSyncIOServer implements SyncIOServer {

    private static final Logger log = LoggerFactory.getLogger(DefaultSyncIOServer.class);

    private final InetSocketAddress address;

    private final AtomicBoolean closed = new AtomicBoolean();

    private ServerSocketChannel serverSocketChannel;

    private ExecutorService executor;

    private CompletableFuture<Void> serverFuture;

    private final LongAdder requestsTotal = new LongAdder();

    private final AtomicLong currentRequests = new AtomicLong();

    private final AtomicLong requestsOK = new AtomicLong();

    private final AtomicLong requestsFailed = new AtomicLong();

    DefaultSyncIOServer(InetSocketAddress address) {
        this.address = Objects.requireNonNull(address, "address");
    }

    @Override
    public SyncIOServer run(Handler handler) {
        try {
            this.serverSocketChannel = ServerSocketChannel.open(StandardProtocolFamily.INET);
            this.serverSocketChannel.bind(address);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.serverFuture = CompletableFuture.runAsync(() -> {
            while (!closed.get()) {
                var socketChannel = newChannel();
                if (socketChannel != null) {
                    var future = CompletableFuture.runAsync(
                            () -> {
                                requestsTotal.increment();
                                currentRequests.incrementAndGet();
                            },
                            executor
                        )
                        .thenRun(() ->
                            handler.run(socketChannel, socketChannel))
                        .whenComplete((_, throwable) -> {
                            try {
                                if (throwable != null) {
                                    log.error("Failed to process request", throwable);
                                    requestsFailed.incrementAndGet();
                                } else {
                                    requestsOK.incrementAndGet();
                                }
                            } finally {
                                currentRequests.decrementAndGet();
                            }
                            try {
                                socketChannel.close();
                            } catch (Exception e) {
                                log.error("Failed to close {}", socketChannel, e);
                            }
                        });
                    log.debug("Processing request from {}: {}", socketChannel, future);
                }
            }
        });
        return this;
    }

    @Override
    public int port() {
        return serverSocketChannel.socket().getLocalPort();
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                serverSocketChannel.close();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to close " + serverSocketChannel, e);
            }
            log.info("{} closed", this);
        }
    }

    @Override
    public void join() {
        serverFuture.join();
    }

    private void closed(Throwable throwable) {
        if (throwable != null) {
        } else {
            requestsOK.incrementAndGet();
        }
        currentRequests.decrementAndGet();
    }

    private SocketChannel newChannel() {
        try {
            return serverSocketChannel.accept();
        } catch (Exception e) {
            if (closed.get()) {
                log.debug("{} closed, did not accept", this, e);
                return null;
            }
            throw new RuntimeException(e);
        }
    }

    private static Runnable runnable(Handler handler, SocketChannel socketChannel) {
        return () ->
            handler.run(socketChannel, socketChannel);
    }
}
