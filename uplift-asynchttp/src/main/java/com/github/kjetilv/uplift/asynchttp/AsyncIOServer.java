package com.github.kjetilv.uplift.asynchttp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Long.MAX_VALUE;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

final class AsyncIOServer implements IOServer {

    private static final Logger log = LoggerFactory.getLogger(AsyncIOServer.class);

    static IOServer server(
        Integer port,
        int requestBufferSize
    ) {
        return new AsyncIOServer(
            new InetSocketAddress(getInetAddress(), port == null || port <= 0 ? 0 : port),
            requestBufferSize > 0 ? requestBufferSize : MINIMUM_REQUEST_SIZE
        );
    }

    private final AsynchronousServerSocketChannel serverSocketChannel;

    private final int requestBufferSize;

    private final AtomicBoolean closed = new AtomicBoolean();

    private final AsynchronousChannelGroup channelGroup;

    private final SocketAddress localAddress;

    private final Lock activeLock = new ReentrantLock();

    private final Condition activeCondition = activeLock.newCondition();

    private final LongAdder readCount = new LongAdder();

    @SuppressWarnings("UnnecessaryToStringCall")
    private AsyncIOServer(InetSocketAddress address, int requestBufferSize) {
        this.requestBufferSize = requestBufferSize;
        try {
            this.channelGroup = AsynchronousChannelProvider.provider()
                .openAsynchronousChannelGroup(Executors.newVirtualThreadPerTaskExecutor(), 0);
            this.serverSocketChannel = AsynchronousServerSocketChannel
                .open(channelGroup)
                .bind(address);
            this.localAddress = this.serverSocketChannel.getLocalAddress();
            log.info("{} bound", this.toString());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to open at " + address, e);
        }
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            doClose();
        }
    }

    @Override
    public void join() {
        awaitTermination(Duration.ZERO);
    }

    @Override
    public InetSocketAddress address() {
        if (localAddress instanceof InetSocketAddress address) {
            return address;
        }
        throw new IllegalStateException("Not a socket address: " + localAddress);
    }

    @Override
    public void awaitActive(Duration timeout) {
        Instant startTime = Instant.now();
        activeLock.lock();
        try {
            while (readCount.longValue() == 0) {
                Duration timeTaken = Duration.between(startTime, Instant.now());
                if (timeTaken.compareTo(timeout) > 0) {
                    return;
                }
                try {
                    activeCondition.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Failed to await", e);
                }
            }
        } finally {
            activeLock.unlock();
        }
    }

    @Override
    public <S extends ChannelState, C extends ChannelHandler<S, C>> IOServer run(
        HandlerProvider<S, C> handlerProvider
    ) {
        if (!closed.get() && serverSocketChannel.isOpen()) {
            serverSocketChannel.accept(null, new ChannelReader<>(handlerProvider));
        }
        return this;
    }

    private boolean awaitTermination(Duration timeout) {
        try {
            if (terminatedWithin(timeout)) {
                log.debug("Terminated: {}", this);
                return true;
            }
            log.warn("Did not terminate within {}: {}", timeout, this);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting {}: {} ", timeout.isZero() ? "forever" : timeout, channelGroup, e);
        }
        return false;
    }

    private boolean terminatedWithin(Duration timeout) throws InterruptedException {
        boolean limited = !timeout.isZero();
        return channelGroup.awaitTermination(
            limited ? timeout.toMillis() : MAX_VALUE,
            limited ? MILLISECONDS : DAYS
        );
    }

    private void doClose() {
        try {
            serverSocketChannel.close();
        } catch (Exception e) {
            throw new IllegalStateException(this + " failed to close: " + serverSocketChannel, e);
        }
        channelGroup.shutdown();
        if (!channelGroup.isTerminated()) {
            if (!awaitTermination(GRACE_PERIOD)) {
                try {
                    channelGroup.shutdownNow();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to terminate: " + channelGroup, e);
                }
            }
        }
    }

    private static final String ALL = "0.0.0.0";

    private static final int MINIMUM_REQUEST_SIZE = 1024;

    private static final Duration GRACE_PERIOD = Duration.ofSeconds(5);

    private static InetAddress getInetAddress() {
        try {
            return InetAddress.getByName(ALL);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to resolve bind address", e);
        }
    }

    private final class ChannelReader<S extends ChannelState, C extends ChannelHandler<S, C>>
        implements CompletionHandler<AsynchronousSocketChannel, Object> {

        private final HandlerProvider<S, C> provider;

        private ChannelReader(HandlerProvider<S, C> provider) {
            try {
                this.provider = provider;
            } finally {
                incrementRead();
            }
        }

        @Override
        public void completed(AsynchronousSocketChannel channel, Object __) {
            requireNonNull(channel, "channel");
            if (closed.get()) {
                handleClosed(channel, null);
                return;
            }
            try {
                if (channel.isOpen()) {
                    read(channel);
                } else {
                    log.warn("Client channel was not open: {}", channel);
                }
            } catch (Exception e) {
                handleClosed(channel, e);
            } finally {
                if (serverSocketChannel.isOpen()) {
                    serverSocketChannel.accept(null, this);
                }
            }
        }

        @Override
        public void failed(Throwable exc, Object attchment) {
            if (closed.get()) {
                log.debug("Closed, did not accept: {} / {}", attchment, exc.toString());
            } else {
                log.error("Failed to accept: {}", attchment, exc);
            }
        }

        private void incrementRead() {
            readCount.increment();
            if (readCount.longValue() == 1) {
                activeLock.lock();
                try {
                    activeCondition.signalAll();
                } finally {
                    activeLock.unlock();
                }
            }
        }

        private void read(AsynchronousSocketChannel channel) {
            ChannelHandler<S, C> handler = provider.handler(channel);
            ByteBuffer buffer = ByteBuffer.allocateDirect(requestBufferSize);
            S state = handler.channelState(buffer);
            channel.read(buffer, state, handler);
        }

        private static void handleClosed(Closeable channel, Exception e) {
            try {
                channel.close();
                if (e != null) {
                    log.error("Processing failed, closed {}", channel, e);
                }
            } catch (Exception ex) {
                if (e != null) {
                    e.addSuppressed(ex);
                }
                log.error("Processing failed, failed to close {}", channel, e == null ? ex : e);
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + localAddress + "]";
    }
}
