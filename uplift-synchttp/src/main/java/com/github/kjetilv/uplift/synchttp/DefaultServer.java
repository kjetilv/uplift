package com.github.kjetilv.uplift.synchttp;

import module java.base;
import com.github.kjetilv.uplift.util.Throwables;
import com.github.kjetilv.uplift.util.Virtuals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DefaultServer implements Server.Open, Server.Init {

    private static final Logger log = LoggerFactory.getLogger(DefaultServer.class);

    private final InetSocketAddress address;

    private final AtomicBoolean closed = new AtomicBoolean();

    private final ServerSocketChannel serverSocketChannel;

    private final CompletableFuture<Void> serverThread;

    private final Processor processor;

    private final LongAdder connectionCount = new LongAdder();

    private final ThreadFactory threadFactory;

    private final Duration readTimeout;

    private final Set<Connection> connections = ConcurrentHashMap.newKeySet();

    private final Thread shutdownHook;

    DefaultServer(InetSocketAddress address, Duration readTimeout) {
        this(
            Objects.requireNonNull(address, "address"),
            readTimeout,
            null,
            null
        );
    }

    private DefaultServer(
        InetSocketAddress address,
        Duration readTimeout,
        ServerSocketChannel serverSocketChannel,
        Processor processor
    ) {
        var name = "server@" + address.getPort();
        this.address = address;
        this.readTimeout = readTimeout == null || readTimeout.isNegative() || readTimeout.isZero()
            ? DEFAULT_READ_TIMEOUT
            : readTimeout;
        this.threadFactory = Virtuals.virtualThreadFactory(name);
        if (serverSocketChannel == null) {
            this.serverSocketChannel = null;
            this.processor = null;
            this.serverThread = null;
            this.shutdownHook = null;
        } else {
            this.serverSocketChannel = serverSocketChannel;
            this.processor = Objects.requireNonNull(processor, "processor");
            this.serverThread = serverFuture(name);
            Virtuals.thread(name + "-timeouts", this::closeTimedOut).start();
            this.shutdownHook = Virtuals.thread(
                name + "-shutdown",
                () -> {
                    log.info("Shutting down {}", this);
                    close();
                }
            );
            Runtime.getRuntime().addShutdownHook(shutdownHook);
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
                    readTimeout,
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
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            } catch (IllegalStateException e) {
                log.debug("Shutdown in progress: {}", this);
            }
            try {
                serverSocketChannel.close();
            } catch (Exception e) {
                throw new IllegalStateException(this + " failed to close " + serverSocketChannel, e);
            }
            connections.forEach(Connection::closeIfReading);
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
            if (e.getMessage() != null && e.getMessage().contains("Permission denied")) {
                return new Servers.Result.Denied<>(address);
            }
            throw new IllegalStateException(this + " could not open server @ " + address, e);
        } catch (Exception e) {
            throw new IllegalStateException(this + " could not open server @ " + address, e);
        }
    }

    private CompletableFuture<Void> serverFuture(String name) {
        return CompletableFuture.runAsync(this::acceptConnections, Virtuals.executor(name));
    }

    /// Accepts connections until the server is closed. A failure to accept, for example when the process
    /// has too many open files, is logged, and accepting continues after a pause.
    private void acceptConnections() {
        while (!closed.get()) {
            try {
                serve(serverSocketChannel.accept());
                connectionCount.increment();
            } catch (ClosedChannelException e) {
                if (!closed.get()) {
                    log.error("{} was closed unexpectedly", this, e);
                }
                return;
            } catch (Exception e) {
                if (closed.get()) {
                    return;
                }
                log.error("{} failed to accept connection", this, e);
                pause(ACCEPT_FAILURE_PAUSE);
            }
        }
    }

    private void serve(SocketChannel channel) {
        try {
            channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        } catch (IOException e) {
            log.debug("Could not set TCP_NODELAY on {}: {}", channel, Throwables.summary(e));
        }
        var connection = new Connection(channel);
        connections.add(connection);
        threadFactory.newThread(() -> {
            try {
                processor.serve(connection, connection);
            } catch (Exception e) {
                if (closed.get() || connection.timedOut()) {
                    log.debug("{} stopped serving {}: {}", this, connection, Throwables.summary(e));
                } else {
                    log.error("{} failed to serve {}", this, connection, e);
                }
            } finally {
                connections.remove(connection);
                connection.finish();
            }
        }).start();
    }

    /// Closes connections where a read has waited longer than the read timeout
    private void closeTimedOut() {
        var timeout = readTimeout.toNanos();
        var interval = Duration.ofNanos(Math.min(timeout / 4, Duration.ofSeconds(1).toNanos()));
        while (!closed.get()) {
            pause(interval);
            var now = System.nanoTime();
            connections.forEach(connection ->
                connection.closeIfTimedOut(now, timeout));
        }
    }

    private static void pause(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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

    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(60);

    private static final Duration ACCEPT_FAILURE_PAUSE = Duration.ofMillis(100);

    @Override
    public String toString() {
        return getClass().getSimpleName() +
               "[" + connectionCount + " connections@" + address +
               "->" + processor +
               (closed.get() ? " CLOSED" : "") +
               "]";
    }
}
