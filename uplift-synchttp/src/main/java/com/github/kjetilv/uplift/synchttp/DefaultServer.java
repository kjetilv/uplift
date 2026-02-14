package com.github.kjetilv.uplift.synchttp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("StatementWithEmptyBody")
final class DefaultServer implements Server {

    private static final Logger log = LoggerFactory.getLogger(DefaultServer.class);

    public static final ExecutorService VIRTUAL = Executors.newVirtualThreadPerTaskExecutor();

    private final InetSocketAddress address;

    private final AtomicBoolean closed = new AtomicBoolean();

    private final ServerSocketChannel serverSocketChannel;

    private final Runnable serverThread;

    private final Processor processor;

    DefaultServer(InetSocketAddress address) {
        this(
            Objects.requireNonNull(address, "address"),
            null,
            null
        );
    }

    private DefaultServer(
        InetSocketAddress address,
        ServerSocketChannel serverSocketChannel,
        Processor processor
    ) {
        this.address = address;

        if (serverSocketChannel == null) {
            this.serverSocketChannel = null;
            this.processor = null;
            this.serverThread = null;
        } else {
            this.serverSocketChannel = serverSocketChannel;
            this.processor = Objects.requireNonNull(processor, "processor");
            this.serverThread = server()::join;
        }
    }

    @Override
    public Server run(Processor processor) {
        requireNotRunning();
        var serverSocketChannel = openServer();
        var address = new InetSocketAddress(
            this.address.getAddress(), serverSocketChannel.socket().getLocalPort()
        );
        return new DefaultServer(address, serverSocketChannel, processor);
    }

    @Override
    public InetSocketAddress address() {
        requireRunning();
        return new InetSocketAddress(address.getAddress(), serverSocketChannel.socket().getLocalPort());
    }

    @Override
    public void close() {
        requireRunning();
        if (closed.compareAndSet(false, true)) {
            try {
                serverSocketChannel.close();
            } catch (Exception e) {
                throw new IllegalStateException(this + " failed to close " + serverSocketChannel, e);
            }
            log.info("{} closed", this);
        }
    }

    @Override
    public void join() {
        requireRunning();
        serverThread.run();
    }

    @SuppressWarnings("resource")
    private ServerSocketChannel openServer() {
        try {
            return ServerSocketChannel.open().bind(address);
        } catch (Exception e) {
            throw new IllegalStateException(this + " could not open server @ " + address, e);
        }
    }

    private CompletableFuture<Void> server() {
        return CompletableFuture.runAsync(() -> {
            while (processSocket(openSocket())) {
            }
        });
    }

    private SocketChannel openSocket() {
        try {
            return serverSocketChannel.accept();
        } catch (AsynchronousCloseException e) {
            if (closed.get()) {
                log.debug("{} closed", this);
                return null;
            }
            throw new IllegalStateException(this + " failed to accept socket channel @ " + serverSocketChannel, e);
        } catch (Exception e) {
            if (closed.get()) {
                log.debug("{} closed, did not accept", this, e);
                return null;
            }
            throw new IllegalStateException(this + " failed to accept socket channel @ " + serverSocketChannel, e);
        }
    }

    private boolean processSocket(SocketChannel channel) {
        if (channel == null) {
            log.info("{} closed listening loop", this);
            return false;
        }
        CompletableFuture.runAsync(processChannel(channel), VIRTUAL)
            .whenComplete((_, throwable) -> {
                try {
                    handleOutcome(channel, throwable);
                } finally {
                    try {
                        channel.close();
                    } catch (Exception e) {
                        log.error("{} failed to close {}", this, channel, e);
                    }
                }
            });
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private Runnable processChannel(SocketChannel channel) {
        return () -> {
            while (channel.isOpen() && processor.process(channel, channel)) {
            }
        };
    }

    private void requireNotRunning() {
        if (this.serverSocketChannel != null) {
            throw new IllegalStateException(this + " is already running");
        }
    }

    private void requireRunning() {
        if (serverSocketChannel == null) {
            throw new IllegalStateException(this + " is not running");
        }
    }

    private void handleOutcome(SocketChannel channel, Throwable throwable) {
        if (throwable != null) {
            log.error("{} failed to process request", this, throwable);
        } else {
            log.debug("{} processed request: {}", this, channel);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[@" + address + " " + (closed.get() ? "open" : "closed") + "]";
    }
}
