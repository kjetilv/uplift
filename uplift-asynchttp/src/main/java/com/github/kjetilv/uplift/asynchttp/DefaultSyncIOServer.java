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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class DefaultSyncIOServer implements SyncIOServer {

    private static final Logger log = LoggerFactory.getLogger(DefaultSyncIOServer.class);

    private final InetSocketAddress address;

    private final int requestBufferSize;

    private final AtomicBoolean closed = new AtomicBoolean();

    private final Lock lock = new ReentrantLock();

    private ServerSocketChannel serverSocketChannel;

    private ExecutorService executor;

    private CompletableFuture<Void> serverFuture;

    private final LongAdder requestsTotal = new LongAdder();

    private final AtomicLong currentRequests = new AtomicLong();

    private final AtomicLong requestsOK = new AtomicLong();

    private final AtomicLong requestsFailed = new AtomicLong();

    DefaultSyncIOServer(InetSocketAddress address, int requestBufferSize) {
        this.address = Objects.requireNonNull(address, "address");
        this.requestBufferSize = requestBufferSize;
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
        serverFuture = CompletableFuture.runAsync(() -> {
            while (!closed.get()) {
                var socketChannel = newChannel();
                var runnable = runnable(handler, socketChannel);
                var accountingFuture = accountingFuture(runnable);
                log.debug("Processing request from {}: {}", socketChannel, accountingFuture);
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
            log.info("{} closed", this);
        }
    }

    @Override
    public void join() {
        serverFuture.join();
    }

    private CompletableFuture<Void> accountingFuture(Runnable runnable) {
        return CompletableFuture.runAsync(started(), executor)
            .thenRunAsync(runnable)
            .whenComplete((_, throwable) -> closed(throwable));
    }

    private SocketChannel newChannel() {
        try {
            return serverSocketChannel.accept();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void closed(Throwable throwable) {
        if (throwable != null) {
            log.error("Failed to process request", throwable);
            requestsFailed.incrementAndGet();
        } else {
            requestsOK.incrementAndGet();
        }
        currentRequests.decrementAndGet();
    }

    private Runnable started() {
        return () -> {
            requestsTotal.increment();
            currentRequests.incrementAndGet();
        };
    }

    @SuppressWarnings("resource")
    private Runnable future(Handler handler) {
        var socketChannel = newChannel();
        return () -> {
            try (socketChannel) {
                handler.run(socketChannel, socketChannel);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to close " + socketChannel, e);
            }
        };
    }

    private static Runnable runnable(Handler handler, SocketChannel socketChannel) {
        return () ->
            handler.run(socketChannel, socketChannel);
    }
}
