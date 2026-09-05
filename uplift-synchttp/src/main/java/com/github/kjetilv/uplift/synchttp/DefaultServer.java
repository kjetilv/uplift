package com.github.kjetilv.uplift.synchttp;

import com.github.kjetilv.uplift.util.Virtuals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

@SuppressWarnings("StatementWithEmptyBody")
final class DefaultServer implements Server {

    private static final Logger log = LoggerFactory.getLogger(DefaultServer.class);

    private final InetSocketAddress address;

    private final AtomicBoolean closed = new AtomicBoolean();

    private final ServerSocketChannel serverSocketChannel;

    private final Runnable serverThread;

    private final Processor processor;

    private final LongAdder requests = new LongAdder();

    private final ThreadFactory threadFactory;

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
        var name = "server@" + address.getPort();
        this.address = address;
        this.threadFactory = Virtuals.virtualThreadFactory(name);
        if (serverSocketChannel == null) {
            this.serverSocketChannel = null;
            this.processor = null;
            this.serverThread = null;
        } else {
            this.serverSocketChannel = serverSocketChannel;
            this.processor = Objects.requireNonNull(processor, "processor");
            this.serverThread = server(name)::join;
        }
    }

    @Override
    public InetSocketAddress address() {
        requireRunning();
        return new InetSocketAddress(address.getAddress(), serverSocketChannel.socket().getLocalPort());
    }

    @Override
    public void join() {
        requireRunning();
        serverThread.run();
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
    public void close() {
        requireRunning();
        if (closed.compareAndSet(false, true)) {
            try {
                serverSocketChannel.close();
            } catch (Exception e) {
                throw new IllegalStateException(this + " failed to close " + serverSocketChannel, e);
            }
            try {
                processor.close();
            } catch (Exception e) {
                throw new IllegalStateException(this + " failed to close " + processor, e);
            }
        }
    }

    @SuppressWarnings("resource")
    private ServerSocketChannel openServer() {
        try {
            return ServerSocketChannel.open().bind(address);
        } catch (Exception e) {
            throw new IllegalStateException(this + " could not open server @ " + address, e);
        }
    }

    private CompletableFuture<Void> server(String name) {
        return CompletableFuture.runAsync(
            () -> {
                while (processSocket(openSocket())) {
                    requests.increment();
                }
            },
            Virtuals.executor(name)
        );
    }

    private SocketChannel openSocket() {
        try {
            return serverSocketChannel.accept();
        } catch (AsynchronousCloseException e) {
            if (closed.get()) {
                return null;
            }
            throw new IllegalStateException(this + " failed to accept socket channel @ " + serverSocketChannel, e);
        } catch (Exception e) {
            if (closed.get()) {
                log.debug("{} was closed, rejected connection", this, e);
                return null;
            }
            throw new IllegalStateException(this + " failed to accept socket channel @ " + serverSocketChannel, e);
        }
    }

    private boolean processSocket(SocketChannel channel) {
        if (channel == null) {
            return false;
        }
        threadFactory.newThread(() -> {
            try {
                while (channel.isOpen() && processor.process(channel, channel)) {
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to process " + channel, e);
            } finally {
                try {
                    channel.close();
                } catch (Exception e) {
                    log.error("{} failed to close {}", this, channel, e);
                }
            }
        }).start();
        return true;
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

    @Override
    public String toString() {
        return getClass().getSimpleName() +
               "[" + requests + "@" + address +
               "->" + processor +
               (closed.get() ? " CLOSED" : "") +
               "]";
    }
}
