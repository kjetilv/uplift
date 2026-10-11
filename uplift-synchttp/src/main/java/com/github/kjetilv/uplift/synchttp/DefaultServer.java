package com.github.kjetilv.uplift.synchttp;

import module java.base;
import com.github.kjetilv.uplift.util.Virtuals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("StatementWithEmptyBody")
final class DefaultServer implements Server.Open, Server.Init {

    private static final Logger log = LoggerFactory.getLogger(DefaultServer.class);

    private final InetSocketAddress address;

    private final AtomicBoolean closed = new AtomicBoolean();

    private final ServerSocketChannel serverSocketChannel;

    private final CompletableFuture<Void> serverThread;

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
            this.serverThread = serverFuture(name);

            Runtime.getRuntime().addShutdownHook(Virtuals.thread(
                name + "-shutdown",
                () -> {
                    log.info("Shutting down {}", this);
                    close();
                }
            ));
        }
    }

    @SuppressWarnings("resource")
    @Override
    public InetSocketAddress address() {
        requireBound();
        return new InetSocketAddress(
            address.getAddress(),
            serverSocketChannel.socket().getLocalPort()
        );
    }

    @SuppressWarnings("resource")
    @Override
    public void join() {
        requireBound().await();
    }

    @SuppressWarnings("resource")
    @Override
    public Servers.Result<Server.Open> run(Processor processor) {
        return requireNotRunning()
            .openNewChannel()
            .map(channel ->
                new DefaultServer(
                    new InetSocketAddress(
                        this.address.getAddress(),
                        channel.socket().getLocalPort()
                    ),
                    channel,
                    processor
                ));
    }

    @Override
    public void close() {
        if (serverSocketChannel == null) {
            return;
        }
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

    private void await() {
        serverThread.join();
    }

    @SuppressWarnings("resource")
    private Servers.Result<ServerSocketChannel> openNewChannel() {
        try {
            return new Servers.Result.OK<>(ServerSocketChannel.open().bind(address));
        } catch (BindException e) {
            if (e.getMessage().contains("Permission denied")) {
                return new Servers.Result.Denied<>(address);
            }
            throw new IllegalStateException(this + " could not open server @ " + address, e);
        } catch (Exception e) {
            throw new IllegalStateException(this + " could not open server @ " + address, e);
        }
    }

    private CompletableFuture<Void> serverFuture(String name) {
        return CompletableFuture.runAsync(
            () -> {
                while (!closed.get() && processSocket(openSocket())) {
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

    private DefaultServer requireNotRunning() {
        if (this.serverSocketChannel != null) {
            throw new IllegalStateException(this + " is already running");
        }
        return this;
    }

    private DefaultServer requireBound() {
        if (serverSocketChannel == null) {
            throw new IllegalStateException(this + " is not running");
        }
        return this;
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
