package com.github.kjetilv.uplift.asynchttp;

import java.io.Closeable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

final class AsyncIOServer implements IOServer {

    private static final Logger log = LoggerFactory.getLogger(AsyncIOServer.class);

    static IOServer server(
        int port,
        int requestBufferSize,
        ExecutorService executorService
    ) {
        return new AsyncIOServer(
            new InetSocketAddress(getInetAddress(), port),
            requireNonNull(executorService, "executorService"),
            requestBufferSize > 0 ? requestBufferSize : MINIMUM_REQUEST_SIZE
        );
    }

    private final AsynchronousServerSocketChannel serverSocketChannel;

    private final int requestBufferSize;

    private final AtomicBoolean closed = new AtomicBoolean();

    private final AsynchronousChannelGroup channelGroup;

    private final SocketAddress localAddress;

    @SuppressWarnings("UnnecessaryToStringCall")
    private AsyncIOServer(InetSocketAddress address, ExecutorService executorService, int requestBufferSize) {
        this.requestBufferSize = requestBufferSize;
        try {
            this.channelGroup = AsynchronousChannelGroup.withThreadPool(
                executorService == null ? ForkJoinPool.commonPool() : executorService
            );
            this.serverSocketChannel = AsynchronousServerSocketChannel.open(channelGroup).bind(address);
            this.localAddress = this.serverSocketChannel.getLocalAddress();
            log.info("{} bound", this.toString());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to open at " + address, e);
        }
    }

    @Override
    public void join() {
        awaitTermination(true);
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                serverSocketChannel.close();
            } catch (Exception e) {
                throw new IllegalStateException(this + " failed to close: " + serverSocketChannel, e);
            }
            channelGroup.shutdown();
            if (!channelGroup.isTerminated()) {
                awaitTermination(false);
            }
        }
    }

    @Override
    public InetSocketAddress address() {
        if (localAddress instanceof InetSocketAddress address) {
            return address;
        }
        throw new IllegalStateException("Not a socket address: " + localAddress);
    }

    @Override
    public <S extends ChannelState, C extends ChannelHandler<S, C>> IOServer run(
        Function<? super AsynchronousByteChannel, ? extends ChannelHandler<S, C>> handler
    ) {
        if (!closed.get() && serverSocketChannel.isOpen()) {
            serverSocketChannel.accept(null, new ChannelReader<S, C>(handler));
        }
        return this;
    }

    @SuppressWarnings("LoopConditionNotUpdatedInsideLoop")
    private void awaitTermination(boolean forever) {
        boolean terminated;
        try {
            do {
                terminated = channelGroup.awaitTermination(seconds(forever), TimeUnit.SECONDS);
                if (terminated) {
                    return;
                }
            } while (forever);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting{}: {} ", forever ? " forever" : "", channelGroup, e);
        }
        log.warn("Did not terminate: {}", this);
    }

    private final class ChannelReader<S extends ChannelState, C extends ChannelHandler<S, C>>
        implements CompletionHandler<AsynchronousSocketChannel, Object> {

        private final Function<? super AsynchronousSocketChannel, ? extends ChannelHandler<S, C>> provider;

        private ChannelReader(Function<? super AsynchronousSocketChannel, ? extends ChannelHandler<S, C>> provider) {
            this.provider = provider;
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

        private void read(AsynchronousSocketChannel channel) {
            ChannelHandler<S, C> asyncHandler = provider.apply(channel);
            ByteBuffer byteBuffer = ByteBuffer.allocate(requestBufferSize);
            S state = asyncHandler.channelState(byteBuffer);
            channel.read(byteBuffer, state, asyncHandler);
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

    private static final String ALL = "0.0.0.0";

    private static final int MINIMUM_REQUEST_SIZE = 1024;

    private static long seconds(boolean forever) {
        return Duration.ofSeconds(forever ? Long.MAX_VALUE : 1).toSeconds();
    }

    private static InetAddress getInetAddress() {
        try {
            return InetAddress.getByName(ALL);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to resolve bind address", e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + localAddress + "]";
    }
}
